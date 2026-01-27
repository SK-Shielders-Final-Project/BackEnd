package org.rookies.zdme.service;

import org.rookies.zdme.dto.admin.StaffAdminLevelUpdateRequest;
import org.rookies.zdme.dto.admin.StaffAdminLevelUpdateResponse;
import org.rookies.zdme.exception.ForbiddenException;
import org.rookies.zdme.exception.NotFoundException;
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminStaffService {

    private final UserRepository userRepository;

    public AdminStaffService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * ✅ 회원 목록 조회 (SUPER_ADMIN만 가능)
     * 반환 형태:
     * [
     *   { "user_id": 1, "email": "...", "admin_lev": 0 },
     *   ...
     * ]
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStaffList(Long adminId) {
        User requester = userRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("admin not found"));

        Integer requesterLev = requester.getAdminLevel();
        if (requesterLev == null || requesterLev != 2) {
            throw new ForbiddenException("only super admin can view staff list");
        }

        // ✅ 전체 유저 조회
        List<User> users = userRepository.findAll();

        System.out.println("GET staffList adminId=" + adminId + ", requesterLev=" + requesterLev);

        // ✅ 프론트에서 쓰기 쉬운 형태로 변환
        return users.stream()
                .map(u -> Map.<String, Object>of(
                        "user_id", u.getUserId(),
                        "email", u.getEmail(),          // User 엔티티에 getEmail() 있어야 함
                        "admin_lev", u.getAdminLevel()  // 0/1/2
                ))
                .collect(Collectors.toList());

    }

    @Transactional
    public StaffAdminLevelUpdateResponse updateAdminLevel(Long adminId, StaffAdminLevelUpdateRequest req) {
        User requester = userRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("admin not found"));

        Integer requesterLev = requester.getAdminLevel();
        if (requesterLev == null || requesterLev != 2) {
            System.out.println("adminId=" + adminId + ", requesterLev=" + requesterLev);
            throw new ForbiddenException("only super admin can update staff permission");
        }

        validateAdminLev(req.getAdmin_level());

        User target = userRepository.findById(req.getUser_id())
                .orElseThrow(() -> new NotFoundException("target user not found"));

        target.setAdminLevel(req.getAdmin_level());
        User saved = userRepository.save(target);

        return StaffAdminLevelUpdateResponse.builder()
                .user_id(saved.getUserId())
                .username(saved.getUsername())
                .admin_level(saved.getAdminLevel())
                .updated_at(saved.getUpdatedAt())
                .build();
    }

    private void validateAdminLev(Integer lev) {
        if (lev == null) throw new IllegalArgumentException("admin_lev is required");
        if (lev < 0 || lev > 2) throw new IllegalArgumentException("admin_lev must be 0, 1, or 2");
    }
}
