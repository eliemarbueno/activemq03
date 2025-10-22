package com.ebueno.controller;

import com.ebueno.messaging.CategoryMessageProducer;
import com.ebueno.model.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST controller for handling Category related operations.
 */
@Path("/categories")
public class CategoryController {
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);
    private final CategoryMessageProducer producer;

    public CategoryController() {
        this.producer = new CategoryMessageProducer();
    }

    /**
     * Create a new category and send it to the ActiveMQ queue.
     *
     * @param category Category to be created
     * @return HTTP response
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response createCategory(Category category) {
        try {
            logger.info("Received request to create category: {}", category.getName());
            producer.sendMessage(category);
            return Response.status(Response.Status.CREATED).entity(category).build();
        } catch (JMSException e) {
            logger.error("Error sending category message", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to process category").build();
        }
    }
}