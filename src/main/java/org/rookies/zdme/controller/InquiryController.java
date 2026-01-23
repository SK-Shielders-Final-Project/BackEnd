package org.rookies.zdme.controller;

import java.util.List;

import org.rookies.zdme.dto.inquiry.InquiryReplyRequest;
import org.rookies.zdme.dto.inquiry.InquiryResponse;
import org.rookies.zdme.dto.inquiry.InquiryWriteRequest;
import org.rookies.zdme.service.InquiryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class InquiryController {

    private final InquiryService inquiryService;

    public InquiryController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    // 문의사항 작성 (사용자)
    @PostMapping("/user/inquiry/write")
    public ResponseEntity<InquiryResponse> write(
            @RequestHeader(name = "X-USER-ID") Long userId,
            @RequestBody InquiryWriteRequest request
    ) {
        InquiryResponse res = inquiryService.write(userId, request);
        return ResponseEntity.ok(res);
    }

    // 사용자 측 문의사항 조회
    @PostMapping("/user/inquiry")
    public ResponseEntity<List<InquiryResponse>> listByUser(
            @RequestHeader(name = "X-USER-ID") Long userId
    ) {
        return ResponseEntity.ok(inquiryService.listByUser(userId));
    }

    // 관리자 측 문의사항 전체 조회
    @PostMapping("/admin/inquiry")
    public ResponseEntity<List<InquiryResponse>> listForAdmin(
            @RequestHeader(name = "X-ADMIN-ID") Long adminId
    ) {
        return ResponseEntity.ok(inquiryService.listAllForAdmin(adminId));
    }

    // 관리자 답변 작성
    @PutMapping("/admin/inquiry")
    public ResponseEntity<InquiryResponse> reply(
            @RequestHeader(name = "X-ADMIN-ID") Long adminId,
            @RequestBody InquiryReplyRequest request
    ) {
        InquiryResponse res = inquiryService.reply(
                adminId,
                request.getInquiry_id(),
                request.getAdmin_reply()
        );
        return ResponseEntity.ok(res);
    }
}
