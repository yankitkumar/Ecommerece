package com.checkoutline.order.repository;

import com.checkoutline.order.outbox.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {
    List<OutboxEvent> findTop100ByPublishedFalseOrderByCreatedAtAsc();
}
