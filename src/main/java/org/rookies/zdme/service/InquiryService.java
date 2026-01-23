package org.rookies.zdme.service;

import java.util.List;
import java.util.stream.Collectors;

import org.rookies.zdme.dto.inquiry.InquiryResponse;
import org.rookies.zdme.dto.inquiry.InquiryWriteRequest;
import org.rookies.zdme.exception.ForbiddenException;
import org.rookies.zdme.exception.NotFoundException;
import org.rookies.zdme.model.entity.File;
import org.rookies.zdme.model.entity.Inquiry;
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.repository.InquiryRepository;
import org.rookies.zdme.repository.UserRepository;
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

        File file = fileService.saveFromFileName(req.getFile()).orElse(null);

        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .title(req.getTitle())
                .content(req.getContent())
                .imageUrl("")     // 스펙에 image_url이 따로 있으나, 현재 요청은 file만 있음
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

    @Transactional(readOnly = true)
    public List<InquiryResponse> listAllForAdmin(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("admin not found"));

        return inquiryRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(inq -> toResponse(inq, admin.getAdminLevel())) // 관리자 조회 응답 admin_lev는 관리자 레벨로 반환
                .collect(Collectors.toList());
    }

    @Transactional
    public InquiryResponse reply(Long adminId, Long inquiryId, String adminReply) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("admin not found"));

        // 권한 체크 제거: admin_level 0이어도 가능하도록 허용한다

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
