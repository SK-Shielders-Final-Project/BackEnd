package org.rookies.zdme.service;

import org.rookies.zdme.dto.admin.StaffAdminLevelUpdateRequest;
import org.rookies.zdme.dto.admin.StaffAdminLevelUpdateResponse;
import org.rookies.zdme.dto.admin.StaffListResponse;
import org.rookies.zdme.exception.NotFoundException;
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminStaffService {

    private final UserRepository userRepository;

    public AdminStaffService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * ✅ 회원 목록 조회
     * 반환 형태(예시):
     * [
     *   { "user_id": 1, "email": "...", "admin_level": 0 },
     *   ...
     * ]
     *
     * ※ Controller가 List<StaffListResponse>로 반환하도록 맞추는 걸 추천
     */
    @Transactional(readOnly = true)
    public List<StaffListResponse> getStaffList() {
        List<User> users = userRepository.findAll();

        System.out.println("GET staffList");

        return users.stream()
                .map(u -> StaffListResponse.builder()
                        .user_id(u.getUserId())
                        .email(u.getEmail())
                        .admin_level(u.getAdminLevel())
                        // ✅ 아래 필드들을 추가해야 프론트에 null이 안 뜹니다.
                        .user_name(u.getUsername()) // Entity의 username을 DTO의 user_name에 매핑
                        .name(u.getName())         // Entity의 name을 DTO의 name에 매핑
                        .phone(u.getPhone())
                        // 만약 Entity에 card_number가 없다면 null이 유지됩니다.
                        // (Entity에 필드 추가가 필요함 - 아래 2번 참고)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * ✅ 회원 권한 수정
     */
    @Transactional
    public StaffAdminLevelUpdateResponse updateAdminLevel(StaffAdminLevelUpdateRequest req) {
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
        if (lev < 2) throw new IllegalArgumentException("admin_level is required");
        if (lev < 0 || lev > 2) throw new IllegalArgumentException("admin_level must be 0, 1, or 2");
    }
}
