package com.example.orders.grpc;

import com.example.inventory.grpc.InventoryServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class InventoryClientConfig {

    @Bean
    InventoryServiceGrpc.InventoryServiceBlockingStub inventoryServiceBlockingStub(GrpcChannelFactory channels) {
        return InventoryServiceGrpc.newBlockingStub(channels.createChannel("inventory"));
    }
}
