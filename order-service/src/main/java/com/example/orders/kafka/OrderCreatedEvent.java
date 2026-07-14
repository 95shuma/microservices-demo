package com.example.orders.kafka;

public record OrderCreatedEvent(String orderId, String sku, int quantity) {
}
