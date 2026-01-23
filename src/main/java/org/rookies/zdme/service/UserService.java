package org.rookies.zdme.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.rookies.zdme.dto.SignupRequest;
import org.rookies.zdme.dto.UpdateUserInfoRequest;
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    // 1. SQL Injection (사용자명 중복 체크 및 사용자 저장)
    // 2. 대량 할당(Mass Assignment)
    // 3. SQL Injection (사용자 저장)
    @Transactional
    public User vulnerableSignup(Map<String, Object> requestData) {
        // 1. SQL Injection (사용자명 중복 체크)
        String username = (String) requestData.get("username");
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("사용자 이름은 필수입니다.");
        }

        String checkUsernameSql = "SELECT user_id, username, name, password, email, phone, card_number, total_point, pass, admin_level, created_at, updated_at FROM users WHERE username = '" + username + "'";
        try {
            List<Object[]> existingUsers = entityManager.createNativeQuery(checkUsernameSql).getResultList();
            if (!existingUsers.isEmpty()) {
                throw new IllegalStateException("이미 존재하는 아이디입니다.");
            }
        } catch (Exception e) {
            throw new RuntimeException("사용자 중복 확인 중 오류 발생: " + e.getMessage(), e);
        }

        // 2. 대량 할당
        User newUser = User.builder()
                .username((String) requestData.get("username"))
                .name((String) requestData.get("name"))
                .email((String) requestData.get("email"))
                .phone((String) requestData.get("phone"))
                .cardNumber((String) requestData.get("card_number"))
                .totalPoint(requestData.get("total_point") instanceof Integer ? (Integer) requestData.get("total_point") : 0)
                .adminLevel(requestData.get("admin_lev") instanceof Integer ? (Integer) requestData.get("admin_lev") : 0) // 공격자가 admin_lev를 보낼 수 있음
                .createdAt(LocalDateTime.now())
                // .updatedAt(null) // 기본값
                .build();
        
        String rawPassword = (String) requestData.get("password");
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);

        // User 엔티티의 'password' 필드 직접 설정
        try {
            Field passwordField = User.class.getDeclaredField("password");
            passwordField.setAccessible(true); // private 필드 접근 허용
            passwordField.set(newUser, encodedPassword);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("User 엔티티의 비밀번호 설정 중 오류 발생", e);
        }

        // 3. SQL Injection (사용자 저장)
        String insertSql = "INSERT INTO users (username, name, password, email, phone, card_number, total_point, admin_level, created_at) VALUES ('" +
                escapeSql(newUser.getUsername()) + "', '" +
                escapeSql(newUser.getName()) + "', '" +
                escapeSql(newUser.getPassword()) + "', '" + // 이미 해싱된 비밀번호
                escapeSql(newUser.getEmail()) + "', '" +
                escapeSql(newUser.getPhone()) + "', '" +
                escapeSql(newUser.getCardNumber()) + "', " +
                newUser.getTotalPoint() + ", " +
                newUser.getAdminLevel() + ", '" +
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(newUser.getCreatedAt()) + "')";

        try {
            entityManager.createNativeQuery(insertSql).executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("사용자 저장 중 오류 발생: " + e.getMessage(), e);
        }

        // 저장된 사용자의 ID를 가져오기 위한 다시 한번 SQL Injection 가능한 쿼리
        String selectAfterInsertSql = "SELECT user_id, username, name, password, email, phone, card_number, total_point, pass, admin_level, created_at, updated_at FROM users WHERE username = '" + username + "'";
        try {
            List<Object[]> result = entityManager.createNativeQuery(selectAfterInsertSql).getResultList();
            if (result.isEmpty()) {
                throw new RuntimeException("저장된 사용자 정보를 찾을 수 없습니다.");
            }
            return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("저장된 사용자를 찾을 수 없습니다."));

        } catch (Exception e) {
            throw new RuntimeException("저장 후 사용자 정보 조회 중 오류 발생: " + e.getMessage(), e);
        }
    }

    private String escapeSql(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''");
    }


    @Transactional
    public User signup(SignupRequest request) { // 기존 안전한 signup 메소드
        userRepository.findByUsername(request.getUsername()).ifPresent(user -> {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        });

        User newUser = User.builder()
                .username(request.getUsername())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .cardNumber(request.getCardNumber())
                .adminLevel(0)
                .totalPoint(0)
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(newUser);
    }

    public void checkUserRole(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        if (user.getAdminLevel() != 0) {
            throw new BadCredentialsException("INVALID_CREDENTIALS");
        }
    }

    public void checkAdminRole(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        if (user.getAdminLevel() == null || user.getAdminLevel() == 0) {
            throw new BadCredentialsException("INVALID_CREDENTIALS");
        }
    }

    public Map<String, Object> getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("user_id", user.getUserId());
        userInfo.put("username", user.getUsername());
        userInfo.put("password", user.getPassword());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("card_number", user.getCardNumber());
        userInfo.put("admin_lev", user.getAdminLevel());
        userInfo.put("point", user.getTotalPoint());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        userInfo.put("created_at", user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : null);
        userInfo.put("updated_at", user.getUpdatedAt() != null ? user.getUpdatedAt().format(formatter) : null);

        return userInfo;
    }

    public boolean verifyPassword(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    @Transactional
    public User changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException("Invalid current password");
        }

        user.changePassword(passwordEncoder.encode(newPassword));
        
        return userRepository.save(user);
    }

    @Transactional
    public User updateUserInfo(String username, UpdateUserInfoRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        user.updateInfo(request.getName(), request.getEmail(), request.getPhone(), request.getCard_number(), request.getAdmin_lev());

        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
