package org.rookies.zdme.repository;

import java.util.List;
import org.rookies.zdme.model.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findAllByUser_UserIdOrderByCreatedAtDesc(Long userId);
    List<Inquiry> findAllByOrderByCreatedAtDesc();
}
