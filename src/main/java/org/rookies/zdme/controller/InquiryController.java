package org.rookies.zdme.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.rookies.zdme.dto.inquiry.*;
import org.rookies.zdme.service.InquiryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class InquiryController {

    private final InquiryService inquiryService;

    public InquiryController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    @GetMapping("/scrap")
    public ResponseEntity<Map<String, String>> scrapUrl(@RequestParam("url") String targetUrl) {
        try {
            // InquiryService의 스크랩 로직 호출
            Map<String, String> scrapData = inquiryService.fetchFullScrapData(targetUrl);
            return ResponseEntity.ok(scrapData);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // --- 기존 Inquiry API ---
    @PostMapping("/user/inquiry/write")
    public ResponseEntity<InquiryResponse> write(@RequestBody InquiryWriteRequest request) {
        return ResponseEntity.ok(inquiryService.write(request));
    }

    @PutMapping("/user/inquiry/modify")
    public ResponseEntity<InquiryModifyResponse> modify(@RequestBody InquiryModifyRequest request) {
        return ResponseEntity.ok(inquiryService.modify(request));
    }

    @GetMapping("/user/inquiry")
    public ResponseEntity<List<InquiryResponse>> listByUser(@RequestParam("user_id") Long userId) {
        return ResponseEntity.ok(inquiryService.listInquiriesByUser(userId));
    }

    @GetMapping("/user/inquiry/{inquiryId}")
    public ResponseEntity<InquiryDetailResponseDto> getInquiryDetails(@PathVariable Long inquiryId) {
        return ResponseEntity.ok(inquiryService.getInquiryDetails(inquiryId));
    }

    @PostMapping("/admin/inquiry")
    public ResponseEntity<List<InquiryResponse>> listForAdmin(@RequestBody AdminInquiryListRequest request) {
        return ResponseEntity.ok(inquiryService.listAllForAdmin(request.getAdmin_level()));
    }

    @PostMapping("/admin/inquiry/detail")
    public ResponseEntity<InquiryDetailResponseDto> getInquiryDetailsForAdmin(@RequestBody AdminInquiryDetailRequest request) {
        return ResponseEntity.ok(inquiryService.getInquiryDetails(request.getInquiry_id()));
    }

    @PutMapping("/admin/inquiry")
    public ResponseEntity<InquiryResponse> reply(@RequestBody InquiryReplyRequest request) {
        return ResponseEntity.ok(inquiryService.reply(request.getInquiry_id(), request.getAdmin_reply(), request.getAdmin_level()));
    }

    @PostMapping("/user/inquiry/delete")
    public ResponseEntity<InquiryDeleteResponse> deleteByUser(@RequestBody InquiryDeleteRequest request) {
        return ResponseEntity.ok(inquiryService.deleteByUser(request.getUser_id(), request.getInquiry_id()));
    }

    @PostMapping("/admin/inquiry/delete")
    public ResponseEntity<InquiryDeleteResponse> deleteByAdmin(@RequestBody InquiryDeleteRequest request) {
        return ResponseEntity.ok(inquiryService.deleteByAdmin(request.getInquiry_id(), request.getAdmin_level()));
    }
}