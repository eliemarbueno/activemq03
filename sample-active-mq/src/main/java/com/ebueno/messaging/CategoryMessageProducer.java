package com.ebueno.messaging;

import com.ebueno.model.Category;

/**
 * Producer for sending Category messages to ActiveMQ queue.
 */
public class CategoryMessageProducer extends AbstractMessageProducer<Category> {
    
    public CategoryMessageProducer() {
        super(Category.class, "queue.category");
    }
}