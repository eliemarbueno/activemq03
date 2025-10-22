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
public abstract class AbstractMessageConsumer<T> implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMessageConsumer.class);

    protected final ActiveMQConfig config;
    protected final Class<T> entityClass;
    protected final String queueName;
    protected final long pollInterval;
    protected volatile boolean running = true;

    protected AbstractMessageConsumer(Class<T> entityClass, String queueProperty, String intervalProperty) {
        this.config = ActiveMQConfig.getInstance();
        this.entityClass = entityClass;
        this.queueName = config.getProperty(queueProperty);
        this.pollInterval = config.getLongProperty(intervalProperty);
    }

    @Override
    public void run() {
        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;

        try {
            connection = config.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(queueName);
            consumer = session.createConsumer(queue);

            while (running) {
                Message message = consumer.receive(pollInterval);
                if (message instanceof TextMessage) {
                    String xmlContent = ((TextMessage) message).getText();
                    T entity = convertFromXml(xmlContent);
                    processMessage(entity);
                }
            }
        } catch (JMSException | JAXBException e) {
            logger.error("Error processing message", e);
        } finally {
            try {
                if (consumer != null) consumer.close();
                if (session != null) session.close();
                if (connection != null) connection.close();
            } catch (JMSException e) {
                logger.error("Error closing JMS resources", e);
            }
        }
    }

    /**
     * Stop the consumer.
     */
    public void stop() {
        running = false;
    }

    /**
     * Convert XML string to entity.
     *
     * @param xmlContent XML string to convert
     * @return Converted entity
     * @throws JAXBException if conversion fails
     */
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