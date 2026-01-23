package org.rookies.zdme.controller;

import org.rookies.zdme.dto.admin.StaffAdminLevelUpdateRequest;
import org.rookies.zdme.dto.admin.StaffAdminLevelUpdateResponse;
import org.rookies.zdme.service.AdminStaffService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminStaffController {

    private final AdminStaffService adminStaffService;

    public AdminStaffController(AdminStaffService adminStaffService) {
        this.adminStaffService = adminStaffService;
    }

    @PutMapping({"/staff", "/staff/"})
    public ResponseEntity<StaffAdminLevelUpdateResponse> updateStaffPermission(
            @RequestHeader(name = "X-ADMIN-ID") Long adminId, // X-ADMIN-ID 헤더 넣는 부분
            @RequestBody StaffAdminLevelUpdateRequest request // response body 받는 부분
    ) {
        return ResponseEntity.ok(adminStaffService.updateAdminLevel(adminId, request));
    }
}
