package com.example.inventory.dto;

public record ReservationResult(boolean approved, String reason, int remainingQuantity) {

    public static ReservationResult approved(int remainingQuantity) {
        return new ReservationResult(true, "OK", remainingQuantity);
    }

    public static ReservationResult rejected(int availableQuantity) {
        return new ReservationResult(false, "INSUFFICIENT_STOCK", availableQuantity);
    }
}
