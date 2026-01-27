package org.rookies.zdme.repository;

import java.util.List;

import org.rookies.zdme.model.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findAllByUser_UserIdOrderByCreatedAtDesc(Long userId);
    List<Inquiry> findAllByOrderByCreatedAtDesc();

    // ✅ 관리자 페이징 조회(최신순)
    Page<Inquiry> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // ✅ "이 fileId가 이 userId의 문의에 붙어있는가" 존재 여부 체크
    boolean existsByFile_FileIdAndUser_UserId(Long fileId, Long userId);
}
