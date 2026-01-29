package org.rookies.zdme.controller;

import lombok.RequiredArgsConstructor;
import org.rookies.zdme.dto.admin.StaffAdminLevelUpdateRequest;
import org.rookies.zdme.dto.admin.StaffAdminLevelUpdateResponse;
import org.rookies.zdme.dto.admin.StaffListResponse;
import org.rookies.zdme.exception.ForbiddenException;
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.repository.UserRepository;
import org.rookies.zdme.security.SecurityUtil;
import org.rookies.zdme.service.AdminStaffService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminStaffController {

    private final AdminStaffService adminStaffService;
    private final UserRepository userRepository;

    @PutMapping({"/staff"})
    public ResponseEntity<?> updateStaffPermission(
            @RequestBody StaffAdminLevelUpdateRequest request
    ) {
        String currentUsername = SecurityUtil.getCurrentUsername();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("사용자가 존재하지 않습니다: " + currentUsername));

        if (currentUser.getAdminLevel() != 2) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "권한이 없습니다.");
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(adminStaffService.updateAdminLevel(request));
    }
    @GetMapping({"/staff"})
    public ResponseEntity<List<StaffListResponse>> getStaffList() {
        return ResponseEntity.ok(adminStaffService.getStaffList());
    }


}
