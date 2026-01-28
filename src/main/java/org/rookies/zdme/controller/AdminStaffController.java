package org.rookies.zdme.controller;

import org.rookies.zdme.dto.admin.StaffAdminLevelUpdateRequest;
import org.rookies.zdme.dto.admin.StaffAdminLevelUpdateResponse;
import org.rookies.zdme.service.AdminStaffService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/admin")
public class AdminStaffController {

    private final AdminStaffService adminStaffService;

    public AdminStaffController(AdminStaffService adminStaffService) {
        this.adminStaffService = adminStaffService;
    }

    @PutMapping({"/staff"})
    public ResponseEntity<StaffAdminLevelUpdateResponse> updateStaffPermission(
            @RequestBody StaffAdminLevelUpdateRequest request // response body 받는 부분
    ) {
        return ResponseEntity.ok(adminStaffService.updateAdminLevel(request));
    }
    @GetMapping({"/staff"})
    public ResponseEntity<List<Map<String, Object>>> getStaffList() {
        return ResponseEntity.ok(adminStaffService.getStaffList());
    }

}
