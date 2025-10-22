package com.ebueno.messaging;

import com.ebueno.config.ActiveMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

/**
 * Abstract base class for JMS message producers.
 *
 * @param <T> Type of entity to be sent
 */
public abstract class AbstractMessageProducer<T> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMessageProducer.class);
    protected final ActiveMQConfig config;
    protected final Class<T> entityClass;
    protected final String queueName;

    protected AbstractMessageProducer(Class<T> entityClass, String queueProperty) {
        this.config = ActiveMQConfig.getInstance();
        this.entityClass = entityClass;
        this.queueName = config.getProperty(queueProperty);
    }

    /**
     * Send entity as XML message to the queue.
     *
     * @param entity Entity to be sent
     * @throws JMSException if message sending fails
     */
    public void sendMessage(T entity) throws JMSException {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        try {
            connection = config.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(queueName);
            producer = session.createProducer(queue);

            String xmlContent = convertToXml(entity);
            TextMessage message = session.createTextMessage(xmlContent);

            producer.send(message);
            logger.info("Sent message to queue {}: {}", queueName, xmlContent);

        } catch (JAXBException e) {
            logger.error("Error converting entity to XML", e);
            throw new JMSException("Failed to convert entity to XML: " + e.getMessage());
        } finally {
            try {
                if (producer != null) producer.close();
                if (session != null) session.close();
                if (connection != null) connection.close();
            } catch (JMSException e) {
                logger.error("Error closing JMS resources", e);
            }
        }
    }

    /**
     * Convert entity to XML string.
     *
     * @param entity Entity to convert
     * @return XML string representation
     * @throws JAXBException if conversion fails
     */
    protected String convertToXml(T entity) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(entityClass);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        StringWriter writer = new StringWriter();
        marshaller.marshal(entity, writer);
        return writer.toString();
    }
}