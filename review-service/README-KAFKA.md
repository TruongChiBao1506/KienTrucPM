# Kafka Integration in Review Service

## Overview
This document describes the Kafka integration in the Review Service. Kafka is used for asynchronous communication between services, making the system more resilient and scalable.

## Kafka Topics
The following Kafka topics are used:

1. **review-created**: Events published when a new review is created
2. **user-info**: Events containing user information
3. **product-info**: Events containing product information

## Implementation Details

### Dependencies
The following dependency was added to the `pom.xml` file:
```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

### Configuration
Kafka is configured in the `KafkaConfig` class and in `application.properties`:

```properties
# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=review-service-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

### Event Classes
The following event classes were created:
- `ReviewCreatedEvent`: Contains information about a newly created review
- `UserInfoEvent`: Contains user information
- `ProductInfoEvent`: Contains product information

### Producer
The `KafkaProducerService` is responsible for sending events to Kafka topics. Currently, it sends `ReviewCreatedEvent` messages to the `review-created` topic when a new review is created.

### Consumer
The `KafkaConsumerService` is responsible for receiving events from Kafka topics. It listens to the `user-info` and `product-info` topics and caches the received information for later use.

### Integration with Review Service
The `ReviewServiceImpl` was updated to:
1. Try to get user and product information from the Kafka consumer's cache first
2. Fall back to synchronous calls if the information is not in the cache
3. Send a `ReviewCreatedEvent` to Kafka when a review is created

## Next Steps for Complete Integration

To fully integrate Kafka into the system, the following steps should be taken:

1. **User Service**:
   - Add Kafka dependencies and configuration
   - Implement a producer to send `UserInfoEvent` messages to the `user-info` topic when user information changes

2. **Product Service**:
   - Add Kafka dependencies and configuration
   - Implement a producer to send `ProductInfoEvent` messages to the `product-info` topic when product information changes

3. **Other Services**:
   - Implement consumers for the `review-created` topic to react to new reviews
   - Update services to use Kafka for other asynchronous communications

## Benefits of Kafka Integration

1. **Resilience**: Services can continue to function even if other services are temporarily unavailable
2. **Scalability**: Services can handle more load by processing messages asynchronously
3. **Loose Coupling**: Services are less dependent on each other
4. **Event Sourcing**: All events are stored in Kafka, enabling event sourcing patterns
5. **Real-time Processing**: Events can be processed in real-time by multiple consumers