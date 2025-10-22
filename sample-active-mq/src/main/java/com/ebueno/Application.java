package com.ebueno;

import com.ebueno.config.ActiveMQConfig;
import com.ebueno.controller.CategoryController;
import com.ebueno.controller.ProductController;
import com.ebueno.messaging.CategoryMessageConsumer;
import com.ebueno.messaging.ProductMessageConsumer;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main application class that starts the REST API server and message consumers.
 */
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static HttpServer server;
    private static ExecutorService executorService;

    /**
     * Start the application.
     *
     * @param args Command line arguments (not used)
     * @throws IOException if server startup fails
     */
    public static void main(String[] args) throws IOException {
        ActiveMQConfig config = ActiveMQConfig.getInstance();
        String host = config.getProperty("rest.api.host");
        String port = config.getProperty("rest.api.port");
        
        // Start REST API server
        URI baseUri = URI.create(String.format("http://%s:%s/api/", host, port));
        ResourceConfig resourceConfig = new ResourceConfig()
                .register(ProductController.class)
                .register(CategoryController.class);
        
        server = GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig);
        logger.info("REST API server started at {}", baseUri);

        // Start message consumers
        executorService = Executors.newFixedThreadPool(2);
        executorService.submit(new ProductMessageConsumer());
        executorService.submit(new CategoryMessageConsumer());
        logger.info("Message consumers started");

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down application...");
            shutdown();
        }));
    }

    /**
     * Shutdown the application gracefully.
     */
    public static void shutdown() {
        if (server != null) {
            server.shutdown();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}