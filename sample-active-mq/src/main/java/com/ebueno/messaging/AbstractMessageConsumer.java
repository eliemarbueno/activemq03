package com.ebueno.messaging;

import com.ebueno.config.ActiveMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.StringReader;

/**
 * Abstract base class for JMS message consumers.
 *
 * @param <T> Type of entity to be received
 */
public abstract class AbstractMessageConsumer<T> implements Runnable, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMessageConsumer.class);

    protected final ActiveMQConfig config;
    protected final Class<T> entityClass;
    protected final String queueName;
    protected final long pollInterval;
    protected volatile boolean running = true;
    protected final ConsumerBehavior behavior;
    
    private volatile Connection connection;
    private volatile Session session;
    private volatile MessageConsumer consumer;
    private volatile Thread consumerThread;

    protected AbstractMessageConsumer(Class<T> entityClass, String queueProperty, String intervalProperty, ConsumerBehavior behavior) {
        this.config = ActiveMQConfig.getInstance();
        this.entityClass = entityClass;
        this.queueName = config.getProperty(queueProperty);
        this.pollInterval = config.getLongProperty(intervalProperty);
        this.behavior = behavior;
    }

    /**
     * Starts the message consumer in a new thread.
     * For SINGLE_READ behavior, the thread will terminate after processing one message.
     * For PERSISTENT behavior, the thread will continue until stop() is called.
     */
    public void start() {
        if (consumerThread != null && consumerThread.isAlive()) {
            logger.warn("Consumer already running");
            return;
        }
        running = true;
        consumerThread = new Thread(this);
        consumerThread.start();
        logger.info("Started {} consumer for queue: {}", 
            behavior == ConsumerBehavior.SINGLE_READ ? "single-read" : "persistent", 
            queueName);
    }

    /**
     * Closes all JMS resources and ensures proper cleanup
     */
    @Override
    public synchronized void close() {
        running = false;
        logger.info("Closing JMS resources for {} consumer on queue: {}", 
            behavior == ConsumerBehavior.SINGLE_READ ? "single-read" : "persistent",
            queueName);
            
        try {
            if (consumer != null) {
                consumer.close();
                consumer = null;
            }
            if (session != null) {
                session.close();
                session = null;
            }
            if (connection != null) {
                connection.close();
                connection = null;
            }
            
            // Wait for the consumer thread to finish
            if (consumerThread != null && consumerThread.isAlive()) {
                consumerThread.join(pollInterval);
                if (consumerThread.isAlive()) {
                    consumerThread.interrupt();
                }
            }
        } catch (Exception e) {
            logger.error("Error closing JMS resources", e);
        } finally {
            consumerThread = null;
        }
    }

    @Override
    public void run() {
        try {
            connection = config.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(queueName);
            consumer = session.createConsumer(queue);

            if (behavior == ConsumerBehavior.SINGLE_READ) {
                // For SINGLE_READ, receive only one message and close immediately
                Message message = consumer.receive(pollInterval);
                if (message instanceof TextMessage) {
                    String xmlContent = ((TextMessage) message).getText();
                    T entity = convertFromXml(xmlContent);
                    processMessage(entity);
                }
                running = false;
                close(); // Close resources immediately after processing
            } else {
                // For PERSISTENT, maintain the receiving loop
                while (running) {
                    Message message = consumer.receive(pollInterval);
                    if (message instanceof TextMessage) {
                        String xmlContent = ((TextMessage) message).getText();
                        T entity = convertFromXml(xmlContent);
                        processMessage(entity);
                    }
                }
            }
        } catch (JMSException | JAXBException e) {
            logger.error("Error processing message", e);
        } finally {
            if (behavior == ConsumerBehavior.SINGLE_READ || !running) {
                close();
            }
        }
    }

    /**
     * Stop the consumer and close all resources.
     */
    public synchronized void stop() {
        if (running) {
            running = false;
            close();
        }
    }

    /**
     * Convert XML string to entity.
     *
     * @param xmlContent XML string to convert
     * @return Converted entity
     * @throws JAXBException if conversion fails
     */
    @SuppressWarnings("unchecked")
    protected T convertFromXml(String xmlContent) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(entityClass);
        return (T) context.createUnmarshaller().unmarshal(new StringReader(xmlContent));
    }

    /**
     * Process the received message.
     *
     * @param entity Entity to process
     */
    protected abstract void processMessage(T entity);
}