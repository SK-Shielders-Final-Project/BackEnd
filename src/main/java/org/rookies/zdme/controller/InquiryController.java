package org.rookies.zdme.controller;

import java.util.List;

import org.rookies.zdme.dto.inquiry.*;
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
            @RequestBody InquiryWriteRequest request
    ) {
        InquiryResponse res = inquiryService.write(request);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/user/inquiry/modify")
    public ResponseEntity<InquiryModifyResponse> modify(
            @RequestBody InquiryModifyRequest request
    ) {
        return ResponseEntity.ok(inquiryService.modify(request));
    }

    // 사용자 측 문의사항 조회
    @PostMapping("/user/inquiry")
    public ResponseEntity<List<InquiryResponse>> listByUser(
            @RequestBody InquiryListRequest request
    ) {
        return ResponseEntity.ok(inquiryService.listInquiriesByUser(request.getUser_id()));
    }

    // 관리자 측 문의사항 전체 조회
    @PostMapping("/admin/inquiry")
    public ResponseEntity<List<InquiryResponse>> listForAdmin(
            @RequestBody AdminInquiryListRequest request
    ) {
        return ResponseEntity.ok(inquiryService.listAllForAdmin(request.getAdmin_level()));
    }

    // 관리자 측 문의사항 개별 조회
    @PostMapping("/admin/inquiry/{inquiry_id}")
    public ResponseEntity<InquiryResponse> getInquiryForAdmin(
            @PathVariable Long inquiry_id,
            @RequestBody AdminInquiryDetailRequest request
    ) {
        InquiryResponse res = inquiryService.getInquiryById(inquiry_id, request.getAdmin_level());
        return ResponseEntity.ok(res);
    }

    // 관리자 답변 작성
    @PutMapping("/admin/inquiry")
    public ResponseEntity<InquiryResponse> reply(
            @RequestBody InquiryReplyRequest request
    ) {
        InquiryResponse res = inquiryService.reply(
                request.getInquiry_id(),
                request.getAdmin_reply(),
                request.getAdmin_level()
        );
        return ResponseEntity.ok(res);
    }

    // 사용자 문의 삭제
    @PostMapping("/user/inquiry/delete")
    public ResponseEntity<InquiryDeleteResponse> deleteByUser(
            @RequestBody InquiryDeleteRequest request
    ) {
        return ResponseEntity.ok(
                inquiryService.deleteByUser(request.getUser_id(), request.getInquiry_id())
        );
    }

    // 관리자 문의 삭제
    @PostMapping("/admin/inquiry/delete")
    public ResponseEntity<InquiryDeleteResponse> deleteByAdmin(
            @RequestBody InquiryDeleteRequest request
    ) {
        return ResponseEntity.ok(
                inquiryService.deleteByAdmin(request.getInquiry_id(), request.getAdmin_level())
        );
    }

}
