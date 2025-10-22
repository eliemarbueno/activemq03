package com.ebueno.messaging;

import com.ebueno.model.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consumer for receiving Category messages from ActiveMQ queue.
 * This consumer processes messages and releases the connection after each read.
 */
public class CategoryMessageConsumer extends AbstractMessageConsumer<Category> {
    private static final Logger logger = LoggerFactory.getLogger(CategoryMessageConsumer.class);

    public CategoryMessageConsumer() {
        super(Category.class, "queue.category", "consumer.category.interval");
    }

    @Override
    protected void processMessage(Category category) {
        logger.info("Processing category message: {}", category.getName());
        // Add your category processing logic here
        stop(); // Release the connection after processing
    }
}