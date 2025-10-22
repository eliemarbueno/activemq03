package com.ebueno.messaging;

import com.ebueno.model.Product;

/**
 * Producer for sending Product messages to ActiveMQ queue.
 */
public class ProductMessageProducer extends AbstractMessageProducer<Product> {
    
    public ProductMessageProducer() {
        super(Product.class, "queue.product");
    }
}