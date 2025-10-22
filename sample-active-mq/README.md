# ActiveMQ Sample Application

This is a Java application that demonstrates integration with ActiveMQ for producing and consuming messages using JMS (Java Message Service). The application provides REST endpoints for sending messages to ActiveMQ queues and implements consumers for processing these messages.

## Requirements

- Java 8
- Maven 3.6+
- Docker (for running ActiveMQ)

## Project Structure

The code is organized into clean, maintainable layers:

- Models (entities)
- Messaging (producers and consumers)
- Controllers (REST API)
- Configuration


```
src/main/java/com/ebueno/
├── Application.java                 # Main application class
├── config/
│   └── ActiveMQConfig.java         # Configuration management
├── controller/
│   ├── CategoryController.java     # REST endpoint for categories
│   └── ProductController.java      # REST endpoint for products
├── messaging/
│   ├── AbstractMessageConsumer.java # Base consumer class
│   ├── AbstractMessageProducer.java # Base producer class
│   ├── CategoryMessageConsumer.java # Category-specific consumer
│   ├── CategoryMessageProducer.java # Category-specific producer
│   ├── ProductMessageConsumer.java  # Product-specific consumer
│   └── ProductMessageProducer.java  # Product-specific producer
└── model/
    ├── Category.java               # Category entity
    └── Product.java                # Product entity
```

## Features

- REST API for sending messages to ActiveMQ queues
- XML message format
- Continuous product message consumer
- One-time category message consumer
- Configurable consumer intervals
- SLF4J logging

## Setup Instructions

1. Start ActiveMQ using Docker:
   ```bash
   docker run -d --name activemq -p 61616:61616 -p 8161:8161 rmohr/activemq:5.15.9
   ```

2. Build the application:
   ```bash
   mvn clean package
   ```

3. Run the application:
   ```bash
   java -jar target/sample-active-mq-1.0-SNAPSHOT.jar
   ```

## Configuration

The application is configured through \`src/main/resources/application.properties\`:

```properties
# ActiveMQ Connection
activemq.broker.url=tcp://localhost:61616
activemq.broker.username=admin
activemq.broker.password=admin

# Queue Names
queue.product=PRODUCT_QUEUE
queue.category=CATEGORY_QUEUE

# Consumer Intervals (in milliseconds)
consumer.product.interval=30000
consumer.category.interval=40000

# REST API Configuration
rest.api.host=0.0.0.0
rest.api.port=8080
```

## Usage Examples

### Send Product Message

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/xml" \
  -d '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
      <product>
          <id>1</id>
          <name>Sample Product</name>
          <description>A sample product description</description>
          <price>29.99</price>
      </product>'
```

### Send Category Message

```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/xml" \
  -d '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
      <category>
          <id>1</id>
          <name>Sample Category</name>
          <description>A sample category description</description>
      </category>'
```

## Integration Details

### Product Integration

1. REST endpoint: \`POST /api/products\`
2. Queue: \`PRODUCT_QUEUE\`
3. Consumer runs continuously
4. Processing interval: 30 seconds

### Category Integration

1. REST endpoint: \`POST /api/categories\`
2. Queue: \`CATEGORY_QUEUE\`
3. Consumer releases connection after processing
4. Processing interval: 40 seconds

## Monitoring

1. Access ActiveMQ admin console:
   - URL: http://localhost:8161/admin
   - Username: admin
   - Password: admin

2. Monitor application logs:
   - The application uses SLF4J with Logback for logging
   - Logs include message processing details and errors

## Architecture

The application follows Clean Architecture and SOLID principles:

1. **Single Responsibility Principle**
   - Each class has a single responsibility
   - Separate classes for producers, consumers, and controllers

2. **Open/Closed Principle**
   - Abstract base classes for producers and consumers
   - Easy to extend for new message types

3. **Liskov Substitution Principle**
   - Concrete implementations properly extend base classes
   - Maintains expected behavior

4. **Interface Segregation**
   - Focused interfaces and classes
   - No unnecessary dependencies

5. **Dependency Inversion**
   - High-level modules don't depend on low-level modules
   - Both depend on abstractions

## Clean Architecture Layers

1. **Entities** (Inner Circle)
   - Product and Category models

2. **Use Cases** (Middle Circle)
   - Message producers and consumers
   - Business logic implementation

3. **Interface Adapters** (Outer Circle)
   - REST controllers
   - Configuration management

4. **Frameworks** (Outermost Circle)
   - ActiveMQ
   - Jersey (JAX-RS)
   - Grizzly HTTP Server

