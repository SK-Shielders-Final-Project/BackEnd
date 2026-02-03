package org.rookies.zdme.repository;

import org.rookies.zdme.model.entity.GiftHistory;
import org.rookies.zdme.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GiftHistoryRepository extends JpaRepository<GiftHistory, Long> {
    List<GiftHistory> findBySenderOrReceiverOrderByCreatedAtDesc(User sender, User receiver);
}
