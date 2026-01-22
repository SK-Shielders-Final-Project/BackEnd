package org.rookies.zdme.repository;

import org.rookies.zdme.model.entity.Payment;
import org.rookies.zdme.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);
    List<Payment> findAllByUser(User user);
}
