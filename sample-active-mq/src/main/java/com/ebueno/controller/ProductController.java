package com.ebueno.controller;

import com.ebueno.messaging.ProductMessageProducer;
import com.ebueno.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST controller for handling Product related operations.
 */
@Path("/products")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductMessageProducer producer;

    public ProductController() {
        this.producer = new ProductMessageProducer();
    }

    /**
     * Create a new product and send it to the ActiveMQ queue.
     *
     * @param product Product to be created
     * @return HTTP response
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response createProduct(Product product) {
        try {
            logger.info("Received request to create product: {}", product.getName());
            producer.sendMessage(product);
            return Response.status(Response.Status.CREATED).entity(product).build();
        } catch (JMSException e) {
            logger.error("Error sending product message", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to process product").build();
        }
    }
}