package com.example.inventory.service;

import com.example.inventory.domain.StockItem;
import com.example.inventory.dto.ReservationResult;
import com.example.inventory.repository.StockItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final StockItemRepository repository;

    @Cacheable(value = "stock", key = "#sku")
    public int getAvailableQuantity(String sku) {
        log.info("Cache miss for sku={}, loading from Postgres", sku);
        return repository.findById(sku).map(StockItem::getQuantity).orElse(0);
    }

    /**
     * Postgres (guarded by @Version optimistic locking) is the source of
     * truth for the decrement; the Redis-backed cache entry is evicted
     * afterward rather than written through, so the next read repopulates it.
     */
    @Transactional
    @CacheEvict(value = "stock", key = "#sku")
    public ReservationResult reserve(String sku, int quantity) {
        StockItem item = repository.findById(sku).orElse(null);
        if (item == null || item.getQuantity() < quantity) {
            int available = item == null ? 0 : item.getQuantity();
            log.warn("Rejecting reservation for sku={} requested={} available={}", sku, quantity, available);
            return ReservationResult.rejected(available);
        }

        item.setQuantity(item.getQuantity() - quantity);
        repository.save(item);
        log.info("Reserved sku={} quantity={} remaining={}", sku, quantity, item.getQuantity());
        return ReservationResult.approved(item.getQuantity());
    }
}
