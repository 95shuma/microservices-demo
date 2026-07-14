package com.example.orders.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final String TOPIC = "order-created";

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {
        // Keyed by orderId so all events for a given order land on the same
        // partition and are consumed in order.
        kafkaTemplate.send(TOPIC, event.orderId(), event);
        log.info("Published order-created event for orderId={}", event.orderId());
    }
}
