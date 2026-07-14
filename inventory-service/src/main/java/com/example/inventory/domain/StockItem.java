package com.example.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stock_items")
@Getter
@Setter
@NoArgsConstructor
public class StockItem {

    @Id
    @Column(nullable = false, updatable = false)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    // Optimistic locking guards against lost updates when concurrent
    // orders race to reserve the same SKU.
    @Version
    private long version;

    public StockItem(String sku, int quantity) {
        this.sku = sku;
        this.quantity = quantity;
    }
}
