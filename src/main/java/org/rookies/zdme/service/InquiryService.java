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
        if (req.getUser_id() == null) {
            throw new IllegalArgumentException("user_id is required");
        }
        User user = userRepository.findById(req.getUser_id())
                .orElseThrow(() -> new NotFoundException("User not found"));

        File file = null;
        if (req.getFile_id() != null) {
            file = fileService.getMeta(req.getFile_id());
        }

        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .title(req.getTitle())
                .content(req.getContent())
                .file(file)
                .adminReply(null)
                .build();

        Inquiry saved = inquiryRepository.save(inquiry);
        return toResponse(saved, 0); // adminLevel 기본값 0
    }

    @Transactional(readOnly = true)
    public List<InquiryResponse> listInquiriesByUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("user_id is required");
        }
        List<Inquiry> list = inquiryRepository.findAllByUser_UserIdOrderByCreatedAtDesc(userId);
        return list.stream()
                .map(inq -> toResponse(inq, 0)) // Default adminLevel
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InquiryResponse> listAllForAdmin(Integer adminLevel) { // adminId parameter removed
        return inquiryRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(inq -> toResponse(inq, adminLevel)) // adminLevel default to 0
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InquiryResponse getInquiryById(Long inquiryId, Integer adminLevel) {
        if (inquiryId == null) {
            throw new IllegalArgumentException("inquiryId is required");
        }
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new NotFoundException("Inquiry not found with id: " + inquiryId));
        return toResponse(inquiry, adminLevel); // Assuming adminLevel is 0 for a standard response.
    }

    @Transactional(readOnly = true)
    public InquiryDetailResponseDto getInquiryDetails(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new NotFoundException("Inquiry not found with id: " + inquiryId));
        return InquiryDetailResponseDto.from(inquiry);
    }

    @Transactional
    public InquiryResponse reply(Long inquiryId, String adminReply, Integer adminLevel) { // adminId parameter removed
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new NotFoundException("inquiry not found"));

        inquiry.setAdminReply(adminReply);
        Inquiry saved = inquiryRepository.save(inquiry);

        return toResponse(saved, adminLevel); // adminLevel default to 0
    }

    @Transactional
    public InquiryModifyResponse modify(InquiryModifyRequest req) {
        if (req == null) throw new IllegalArgumentException("request is required");
        if (req.getUser_id() == null) throw new IllegalArgumentException("user_id is required");
        if (req.getInquiry_id() == null) throw new IllegalArgumentException("inquiry_id is required");
        if (req.getTitle() == null || req.getTitle().isBlank()) throw new IllegalArgumentException("title is required");
        if (req.getContent() == null) req.setContent("");

        if (!inquiryRepository.existsByInquiryIdAndUser_UserId(req.getInquiry_id(), req.getUser_id())) {
            throw new ForbiddenException("You are not the owner of this inquiry.");
        }

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

    // InquiryService.java 수정안
    private InquiryResponse toResponse(Inquiry inq, Integer adminLevForResponse) {
        return InquiryResponse.builder()
                .inquiry_id(inq.getInquiryId())
                // 작성자(User)가 null인 경우를 대비해 안전하게 처리
                .user_id(inq.getUser() != null ? inq.getUser().getUserId() : null)
                .title(inq.getTitle())
                // 파일(File)이 null인 경우를 대비해 안전하게 처리
                .file_id(inq.getFile() != null ? inq.getFile().getFileId() : null)
                .admin_level(adminLevForResponse != null ? adminLevForResponse : 0)
                .admin_reply(inq.getAdminReply())
                .created_at(inq.getCreatedAt())
                .updated_at(inq.getUpdatedAt())
                .build();
    }
    @Transactional
    public InquiryDeleteResponse deleteByUser(Long userId, Long inquiryId) {
        if (userId == null) throw new IllegalArgumentException("user_id is required");
        if (inquiryId == null) throw new IllegalArgumentException("inquiry_id is required");

        if (!inquiryRepository.existsByInquiryIdAndUser_UserId(inquiryId, userId)) {
            throw new ForbiddenException("You are not the owner of this inquiry.");
        }

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new NotFoundException("inquiry not found"));

        inquiryRepository.delete(inquiry);

        return InquiryDeleteResponse.builder()
                .result("Y")
                .build();
    }

    @Transactional
    public InquiryDeleteResponse deleteByAdmin(Long inquiryId, Integer adminLevel) { // adminId parameter and validation removed
        if (inquiryId == null) throw new IllegalArgumentException("inquiry_id is required");

        // TODO: Add business logic for adminLevel validation if needed

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new NotFoundException("inquiry not found"));

        inquiryRepository.delete(inquiry);

        return InquiryDeleteResponse.builder()
                .result("Y")
                .build();
    }
}

