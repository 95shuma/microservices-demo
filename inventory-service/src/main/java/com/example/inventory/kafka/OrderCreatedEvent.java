package com.example.inventory.kafka;

/**
 * Mirrors the event shape published by order-service on the "order-created"
 * topic. Kept as a hand-copied DTO rather than a shared library, matching the
 * proto file duplication approach used for the gRPC contract in this demo.
 */
public record OrderCreatedEvent(String orderId, String sku, int quantity) {
}
