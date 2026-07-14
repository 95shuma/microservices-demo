package com.example.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Async projection of orders this service has observed via Kafka, keyed by
 * orderId so re-delivery is a no-op. Stock itself is decremented
 * synchronously through the gRPC ReserveStock call, not from this event —
 * this table exists to demonstrate the event-consumption side of the
 * architecture (e.g. for audit/reporting) without double-decrementing stock.
 */
@Entity
@Table(name = "order_audit")
@Getter
@Setter
@NoArgsConstructor
public class OrderAudit {

    @Id
    @Column(nullable = false, updatable = false)
    private String orderId;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private Instant receivedAt;

    public OrderAudit(String orderId, String sku, int quantity, Instant receivedAt) {
        this.orderId = orderId;
        this.sku = sku;
        this.quantity = quantity;
        this.receivedAt = receivedAt;
    }
}
