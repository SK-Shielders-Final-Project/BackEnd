package org.rookies.zdme.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

    /**
     * [SSRF 취약 로직] 사용자가 입력한 URL의 메타데이터를 가져옵니다.
     * 사설 IP(127.0.0.1, 192.168.x.x)나 클라우드 메타데이터 주소(169.254.169.254)에 대한
     * 필터링이 전혀 없어 서버 권한으로 내부망을 공격할 수 있습니다.
     */
    @Transactional(readOnly = true)
    public Map<String, String> fetchFullScrapData(String targetUrl) {
        Map<String, String> scrapData = new HashMap<>();

        try {
            Document doc = Jsoup.connect(targetUrl)
                    .timeout(5000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .ignoreContentType(true)
                    .get();

            // 1. Title 추출
            String title = doc.select("meta[property=og:title]").attr("content");
            if (title.isEmpty()) title = doc.title();
            scrapData.put("title", title);

            // 2. Description 추출
            String description = doc.select("meta[property=og:description]").attr("content");
            if (description.isEmpty()) {
                description = doc.select("meta[name=description]").attr("content");
            }
            if (description.isEmpty()) {
                String bodyText = doc.body().text();
                description = bodyText.length() > 200 ? bodyText.substring(0, 200) + "..." : bodyText;
            }
            scrapData.put("description", description);

            // 3. Image 추출 (og:image) - 추가된 부분
            String image = doc.select("meta[property=og:image]").attr("content");
            scrapData.put("image", image); // 이미지가 없으면 빈 문자열이 들어감

            // 4. URL 및 전체 본문 데이터
            scrapData.put("url", targetUrl);
            scrapData.put("content", doc.body().html());

        } catch (Exception e) {
            throw new RuntimeException("Scraping failed: " + e.getMessage());
        }

        return scrapData;
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
        return toResponse(saved, 0);
    }

    @Transactional(readOnly = true)
    public List<InquiryResponse> listInquiriesByUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("user_id is required");
        }
        List<Inquiry> list = inquiryRepository.findAllByUser_UserIdOrderByCreatedAtDesc(userId);
        return list.stream()
                .map(inq -> toResponse(inq, 0))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InquiryResponse> listAllForAdmin(Integer adminLevel) {
        return inquiryRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(inq -> toResponse(inq, adminLevel))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InquiryResponse getInquiryById(Long inquiryId, Integer adminLevel) {
        if (inquiryId == null) {
            throw new IllegalArgumentException("inquiryId is required");
        }
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new NotFoundException("Inquiry not found with id: " + inquiryId));
        return toResponse(inquiry, adminLevel);
    }

    @Transactional(readOnly = true)
    public InquiryDetailResponseDto getInquiryDetails(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new NotFoundException("Inquiry not found with id: " + inquiryId));
        return InquiryDetailResponseDto.from(inquiry);
    }

    @Transactional
    public InquiryResponse reply(Long inquiryId, String adminReply, Integer adminLevel) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new NotFoundException("inquiry not found"));

        inquiry.setAdminReply(adminReply);
        Inquiry saved = inquiryRepository.save(inquiry);

        return toResponse(saved, adminLevel);
    }

    @Transactional
    public InquiryModifyResponse modify(InquiryModifyRequest req) {
        if (req == null) throw new IllegalArgumentException("request is required");
        if (req.getUser_id() == null) throw new IllegalArgumentException("user_id is required");
        if (req.getInquiry_id() == null) throw new IllegalArgumentException("inquiry_id is required");
        if (req.getTitle() == null || req.getTitle().isBlank()) throw new IllegalArgumentException("title is required");

        if (!inquiryRepository.existsByInquiryIdAndUser_UserId(req.getInquiry_id(), req.getUser_id())) {
            throw new ForbiddenException("You are not the owner of this inquiry.");
        }

        Inquiry inquiry = inquiryRepository.findById(req.getInquiry_id())
                .orElseThrow(() -> new NotFoundException("inquiry not found"));

        inquiry.setTitle(req.getTitle());
        inquiry.setContent(req.getContent());

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
        return InquiryResponse.builder()
                .inquiry_id(inq.getInquiryId())
                .user_id(inq.getUser() != null ? inq.getUser().getUserId() : null)
                .title(inq.getTitle())
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
    public InquiryDeleteResponse deleteByAdmin(Long inquiryId, Integer adminLevel) {
        if (inquiryId == null) throw new IllegalArgumentException("inquiry_id is required");

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new NotFoundException("inquiry not found"));

        inquiryRepository.delete(inquiry);

        return InquiryDeleteResponse.builder()
                .result("Y")
                .build();
    }
}