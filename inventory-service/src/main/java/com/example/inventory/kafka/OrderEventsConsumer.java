package com.example.inventory.kafka;

import com.example.inventory.domain.OrderAudit;
import com.example.inventory.repository.OrderAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventsConsumer {

    private final OrderAuditRepository orderAuditRepository;

    @KafkaListener(topics = "order-created", groupId = "inventory-service")
    public void onOrderCreated(OrderCreatedEvent event) {
        if (orderAuditRepository.existsById(event.orderId())) {
            log.info("Ignoring duplicate delivery of order-created event for orderId={}", event.orderId());
            return;
        }
        orderAuditRepository.save(new OrderAudit(event.orderId(), event.sku(), event.quantity(), Instant.now()));
        log.info("Recorded audit entry for orderId={} sku={} quantity={}", event.orderId(), event.sku(), event.quantity());
    }
}
