package com.example.orders.dto;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String sku,
        int quantity,
        OrderStatus status,
        String rejectionReason,
        Instant createdAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getSku(),
                order.getQuantity(),
                order.getStatus(),
                order.getRejectionReason(),
                order.getCreatedAt()
        );
    }
}
