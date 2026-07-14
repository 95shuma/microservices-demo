package com.example.inventory.repository;

import com.example.inventory.domain.OrderAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderAuditRepository extends JpaRepository<OrderAudit, String> {
}
