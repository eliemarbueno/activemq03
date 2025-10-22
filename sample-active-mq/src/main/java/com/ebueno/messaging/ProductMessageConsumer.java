package com.ebueno.messaging;

import com.ebueno.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consumer for receiving Product messages from ActiveMQ queue.
 * This consumer runs continuously and processes messages as they arrive.
 */
public class ProductMessageConsumer extends AbstractMessageConsumer<Product> {
    private static final Logger logger = LoggerFactory.getLogger(ProductMessageConsumer.class);

    public ProductMessageConsumer(ConsumerBehavior behavior) {
        super(Product.class, "queue.product", "consumer.product.interval", behavior);
    }

    public ProductMessageConsumer() {
        this(ConsumerBehavior.PERSISTENT); // Default to persistent behavior
    }

    @Override
    protected void processMessage(Product product) {
        logger.info("Processing product message: {}", product.getName());
        // Add your product processing logic here
    }
}