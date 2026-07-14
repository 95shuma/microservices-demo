package com.example.inventory.grpc;

import com.example.inventory.dto.ReservationResult;
import com.example.inventory.service.InventoryService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class InventoryGrpcService extends InventoryServiceGrpc.InventoryServiceImplBase {

    private final InventoryService inventoryService;

    @Override
    public void reserveStock(ReserveStockRequest request, StreamObserver<ReserveStockResponse> responseObserver) {
        ReservationResult result = inventoryService.reserve(request.getSku(), request.getQuantity());
        ReserveStockResponse response = ReserveStockResponse.newBuilder()
                .setApproved(result.approved())
                .setReason(result.reason())
                .setRemainingQuantity(result.remainingQuantity())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getStock(GetStockRequest request, StreamObserver<GetStockResponse> responseObserver) {
        int quantity = inventoryService.getAvailableQuantity(request.getSku());
        GetStockResponse response = GetStockResponse.newBuilder()
                .setSku(request.getSku())
                .setAvailableQuantity(quantity)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
