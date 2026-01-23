package org.rookies.zdme.service;

import org.rookies.zdme.dto.admin.StaffAdminLevelUpdateRequest;
import org.rookies.zdme.dto.admin.StaffAdminLevelUpdateResponse;
import org.rookies.zdme.exception.ForbiddenException;
import org.rookies.zdme.exception.NotFoundException;
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminStaffService {

    private final UserRepository userRepository;

    public AdminStaffService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public StaffAdminLevelUpdateResponse updateAdminLevel(Long adminId, StaffAdminLevelUpdateRequest req) {
        User requester = userRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("admin not found"));

        Integer requesterLev = requester.getAdminLevel();
        if (requesterLev == null || requesterLev != 2) {
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
