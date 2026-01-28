package org.rookies.zdme.service;

import java.util.List;
import java.util.stream.Collectors;

import org.rookies.zdme.dto.inquiry.*;
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
    public InquiryResponse write(InquiryWriteRequest req) {
        File file = null;
        if (req.getFile_id() != null) {
            file = fileService.getMeta(req.getFile_id());
        }

        Inquiry inquiry = Inquiry.builder()
                .user(null) // userId 검증 제거로 인해 user 연결 없음
                .title(req.getTitle())
                .content(req.getContent())
                .imageUrl("")
                .file(file)
                .adminReply(null)
                .build();

        Inquiry saved = inquiryRepository.save(inquiry);
        return toResponse(saved, 0); // adminLevel 기본값 0
    }

    @Transactional(readOnly = true)
    public List<InquiryResponse> listAllInquiries() { // Changed from listByUser
        List<Inquiry> list = inquiryRepository.findAllByOrderByCreatedAtDesc(); // Fetch all
        return list.stream()
                .map(inq -> toResponse(inq, 0)) // Default adminLevel
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InquiryResponse> listAllForAdmin() { // adminId parameter removed
        return inquiryRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(inq -> toResponse(inq, 0)) // adminLevel default to 0
                .collect(Collectors.toList());
    }

    @Transactional
    public InquiryResponse reply(Long inquiryId, String adminReply) { // adminId parameter removed
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new NotFoundException("inquiry not found"));

        inquiry.setAdminReply(adminReply);
        Inquiry saved = inquiryRepository.save(inquiry);

        return toResponse(saved, 0); // adminLevel default to 0
    }

    @Transactional
    public InquiryModifyResponse modify(InquiryModifyRequest req) { // userId parameter and ownership validation removed
        if (req == null) throw new IllegalArgumentException("request is required");
        if (req.getInquiry_id() == null) throw new IllegalArgumentException("inquiry_id is required");
        if (req.getTitle() == null || req.getTitle().isBlank()) throw new IllegalArgumentException("title is required");
        if (req.getContent() == null) req.setContent("");

        Inquiry inquiry = inquiryRepository.findById(req.getInquiry_id())
                .orElseThrow(() -> new NotFoundException("inquiry not found"));

        inquiry.setTitle(req.getTitle());
        inquiry.setContent(req.getContent());

        // file_id 정책: null이면 제거, 값 있으면 교체
        if (req.getFile_id() == null) {
            inquiry.setFile(null);
        } else {
            File file = fileService.getMeta(req.getFile_id());
            inquiry.setFile(file);
        }

        inquiryRepository.save(inquiry);

        return InquiryModifyResponse.builder()
                .result("Y")
                .build();
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

    @Transactional
    public InquiryDeleteResponse deleteByUser(Long inquiryId) { // userId parameter and ownership validation removed
        if (inquiryId == null) throw new IllegalArgumentException("inquiry_id is required");

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new NotFoundException("inquiry not found"));

        inquiryRepository.delete(inquiry);

        return InquiryDeleteResponse.builder()
                .result("Y")
                .build();
    }

    @Transactional
    public InquiryDeleteResponse deleteByAdmin(Long inquiryId) { // adminId parameter and validation removed
        if (inquiryId == null) throw new IllegalArgumentException("inquiry_id is required");

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new NotFoundException("inquiry not found"));

        inquiryRepository.delete(inquiry);

        return InquiryDeleteResponse.builder()
                .result("Y")
                .build();
    }

}
