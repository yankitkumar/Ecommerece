package com.checkoutline.notification.repository;

import com.checkoutline.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByUserIdOrderBySentAtDesc(String userId);
}
