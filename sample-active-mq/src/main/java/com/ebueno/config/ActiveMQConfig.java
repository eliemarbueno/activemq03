package com.ebueno.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration class for ActiveMQ connection and properties management.
 */
public class ActiveMQConfig {
    private static final Logger logger = LoggerFactory.getLogger(ActiveMQConfig.class);
    private static Properties properties;
    private static ActiveMQConfig instance;

    private ActiveMQConfig() {
        loadProperties();
    }

    /**
     * Get singleton instance of ActiveMQConfig.
     *
     * @return ActiveMQConfig instance
     */
    public static synchronized ActiveMQConfig getInstance() {
        if (instance == null) {
            instance = new ActiveMQConfig();
        }
        return instance;
    }

    /**
     * Load application properties from application.properties file.
     */
    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IOException("Unable to find application.properties");
            }
            properties.load(input);
            logger.info("Properties loaded successfully");
        } catch (IOException e) {
            logger.error("Error loading properties file", e);
            throw new RuntimeException("Failed to load properties", e);
        }
    }

    /**
     * Create a new ActiveMQ connection.
     *
     * @return JMS Connection
     * @throws JMSException if connection creation fails
     */
    public Connection createConnection() throws JMSException {
        String brokerUrl = properties.getProperty("activemq.broker.url");
        String username = properties.getProperty("activemq.broker.username");
        String password = properties.getProperty("activemq.broker.password");

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        connectionFactory.setUserName(username);
        connectionFactory.setPassword(password);

        Connection connection = connectionFactory.createConnection();
        logger.info("Created new ActiveMQ connection to {}", brokerUrl);
        return connection;
    }

    /**
     * Get property value by key.
     *
     * @param key Property key
     * @return Property value
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Get property value as long.
     *
     * @param key Property key
     * @return Property value as long
     */
    public long getLongProperty(String key) {
        return Long.parseLong(properties.getProperty(key));
    }
}