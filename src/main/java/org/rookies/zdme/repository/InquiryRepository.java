package org.rookies.zdme.repository;

import java.util.List;

import org.rookies.zdme.model.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findAllByUser_UserIdOrderByCreatedAtDesc(Long userId);
    List<Inquiry> findAllByOrderByCreatedAtDesc();

    // ✅ 수정 권한 검증(해당 inquiry가 이 user 소유인가)
    boolean existsByInquiryIdAndUser_UserId(Long inquiryId, Long userId);

    // (기존에 너가 쓰던 것)
    boolean existsByFile_FileIdAndUser_UserId(Long fileId, Long userId);
}
