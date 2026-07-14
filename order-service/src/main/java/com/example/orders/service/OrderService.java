package com.example.orders.service;

import com.example.inventory.grpc.InventoryServiceGrpc;
import com.example.inventory.grpc.ReserveStockRequest;
import com.example.inventory.grpc.ReserveStockResponse;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderStatus;
import com.example.orders.dto.CreateOrderRequest;
import com.example.orders.kafka.OrderCreatedEvent;
import com.example.orders.kafka.OrderEventProducer;
import com.example.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;
    private final InventoryServiceGrpc.InventoryServiceBlockingStub inventoryStub;

    @Transactional
    public Order placeOrder(CreateOrderRequest request) {
        ReserveStockResponse reservation = inventoryStub.reserveStock(
                ReserveStockRequest.newBuilder()
                        .setSku(request.sku())
                        .setQuantity(request.quantity())
                        .build());

        Order order = reservation.getApproved()
                ? new Order(request.sku(), request.quantity(), OrderStatus.CREATED, null)
                : new Order(request.sku(), request.quantity(), OrderStatus.REJECTED, reservation.getReason());

        order = orderRepository.save(order);

        if (reservation.getApproved()) {
            orderEventProducer.publishOrderCreated(
                    new OrderCreatedEvent(order.getId().toString(), order.getSku(), order.getQuantity()));
        } else {
            log.info("Order rejected for sku={} quantity={} reason={}",
                    request.sku(), request.quantity(), reservation.getReason());
        }

        return order;
    }
}
