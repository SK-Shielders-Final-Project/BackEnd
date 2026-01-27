package org.rookies.zdme.service;

import java.util.List;
import java.util.stream.Collectors;

import org.rookies.zdme.dto.inquiry.InquiryResponse;
import org.rookies.zdme.dto.inquiry.InquiryWriteRequest;
import org.rookies.zdme.exception.NotFoundException;
import org.rookies.zdme.model.entity.File;
import org.rookies.zdme.model.entity.Inquiry;
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.repository.InquiryRepository;
import org.rookies.zdme.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    public InquiryService(
            InquiryRepository inquiryRepository,
            UserRepository userRepository,
            FileService fileService
    ) {
        this.inquiryRepository = inquiryRepository;
        this.userRepository = userRepository;
        this.fileService = fileService;
    }

    @Transactional
    public InquiryResponse write(Long userId, InquiryWriteRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user not found"));

        File file = null;
        if (req.getFile_id() != null) {
            file = fileService.getMeta(req.getFile_id());
        }

        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .title(req.getTitle())
                .content(req.getContent())
                .imageUrl("")
                .file(file)
                .adminReply(null)
                .build();

        Inquiry saved = inquiryRepository.save(inquiry);
        return toResponse(saved, user.getAdminLevel());
    }

    @Transactional(readOnly = true)
    public List<InquiryResponse> listByUser(Long userId) {
        List<Inquiry> list = inquiryRepository.findAllByUser_UserIdOrderByCreatedAtDesc(userId);
        return list.stream()
                .map(inq -> toResponse(inq, inq.getUser().getAdminLevel()))
                .collect(Collectors.toList());
    }

    // ====== 기존 관리자 전체 조회(그대로 유지) ======
    @Transactional(readOnly = true)
    public List<InquiryResponse> listAllForAdmin(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("admin not found"));

        return inquiryRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(inq -> toResponse(inq, admin.getAdminLevel()))
                .collect(Collectors.toList());
    }

    // ====== ✅ 관리자 페이징 조회 (신규) ======
    @Transactional(readOnly = true)
    public Page<InquiryResponse> listAllForAdminPaged(Long adminId, int page, int size) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("admin not found"));

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        Page<Inquiry> result = inquiryRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(safePage, safeSize));

        return result.map(inq -> toResponse(inq, admin.getAdminLevel()));
    }

    // ====== ✅ 관리자 단건 조회 (신규) ======
    @Transactional(readOnly = true)
    public InquiryResponse getOneForAdmin(Long adminId, Long inquiryId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("admin not found"));

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new NotFoundException("inquiry not found"));

        return toResponse(inquiry, admin.getAdminLevel());
    }

    @Transactional
    public InquiryResponse reply(Long adminId, Long inquiryId, String adminReply) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("admin not found"));

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new NotFoundException("inquiry not found"));

        inquiry.setAdminReply(adminReply);
        Inquiry saved = inquiryRepository.save(inquiry);

        return toResponse(saved, admin.getAdminLevel());
    }

    private InquiryResponse toResponse(Inquiry inq, Integer adminLevForResponse) {
        Long fileId = (inq.getFile() == null) ? null : inq.getFile().getFileId();

        return InquiryResponse.builder()
                .inquiry_id(inq.getInquiryId())
                .user_id(inq.getUser() == null ? null : inq.getUser().getUserId())
                .title(inq.getTitle())
                .content(inq.getContent())
                .image_url(inq.getImageUrl() == null ? "" : inq.getImageUrl())
                .file_id(fileId)
                .admin_level(adminLevForResponse == null ? 0 : adminLevForResponse)
                .admin_reply(inq.getAdminReply())
                .created_at(inq.getCreatedAt())
                .updated_at(inq.getUpdatedAt())
                .build();
    }
}
