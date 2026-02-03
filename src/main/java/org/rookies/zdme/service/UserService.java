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

import javax.crypto.Cipher;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @PersistenceContext
    private EntityManager entityManager;

    private void validateUsername(String username) {
        if (username == null || !Pattern.matches("^[a-zA-Z0-9]{5,20}$", username)) {
            throw new IllegalArgumentException("아이디는 5자에서 20자 사이의 영문, 숫자만 가능합니다.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }
        int categoryCount = 0;
        if (Pattern.compile("[a-zA-Z]").matcher(password).find()) categoryCount++;
        if (Pattern.compile("[0-9]").matcher(password).find()) categoryCount++;
        if (Pattern.compile("[^a-zA-Z0-9]").matcher(password).find()) categoryCount++;
        if (categoryCount < 2) { // 영어, 숫자, 특수문자 중 2종류 이상
            throw new IllegalArgumentException("비밀번호는 영문, 숫자, 특수문자 중 2종류를 조합하여 8자 이상이어야 합니다.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || !Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", email)) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다.");
        }
    }

    private void validatePhone(String phone) {
        if (phone == null || !Pattern.matches("^010[0-9]{8}$", phone)) {
            throw new IllegalArgumentException("유효하지 않은 전화번호 형식입니다. (01000000000 형식)");
        }
    }

    // 1. SQL Injection (사용자명 중복 체크 및 사용자 저장)
    // 2. 대량 할당(Mass Assignment)
    // 3. SQL Injection (사용자 저장)
    @Transactional
    public User vulnerableSignup(Map<String, Object> requestData) {
        // 1. SQL Injection (사용자명 중복 체크)
        String username = (String) requestData.get("username");
//        validateUsername(username);

        String checkUsernameSql = "SELECT user_id FROM users WHERE username = '" + username + "'";

        List<Object[]> existingUsers = entityManager.createNativeQuery(checkUsernameSql).getResultList();
        if (!existingUsers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }

        String email = (String) requestData.get("email");
        validateEmail(email);

        if (!email.trim().isEmpty()) {
            String checkEmailSql = "SELECT user_id FROM users WHERE email = '" + email + "'";

            List<Object[]> existingEmails = entityManager.createNativeQuery(checkEmailSql).getResultList();
            if (!existingEmails.isEmpty()) {
                throw new IllegalStateException("이미 존재하는 이메일입니다.");
            }
        }

        String phone = (String) requestData.get("phone");
        validatePhone(phone);

        if (!phone.trim().isEmpty()) {
            String checkPhoneSql = "SELECT user_id FROM users WHERE phone = '" + phone + "'";

            List<Object[]> existingPhones = entityManager.createNativeQuery(checkPhoneSql).getResultList();
            if (!existingPhones.isEmpty()) {
                throw new IllegalStateException("이미 존재하는 휴대폰 번호입니다.");
            }
        }

        // 2. 대량 할당
        User newUser = User.builder()
                .username((String) requestData.get("username"))
                .name((String) requestData.get("name"))
                .email((String) requestData.get("email"))
                .phone((String) requestData.get("phone"))
                .totalPoint(requestData.get("total_point") instanceof Long ? (Long) requestData.get("total_point") : 0)
                .adminLevel(requestData.get("admin_lev") instanceof Integer ? (Integer) requestData.get("admin_lev") : 0) // 공격자가 admin_lev를 보낼 수 있음
                .createdAt(LocalDateTime.now())
                .build();
        
        String rawPassword = (String) requestData.get("password");
        validatePassword(rawPassword);

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
        String insertSql = "INSERT INTO users (username, name, password, email, phone, total_point, admin_level, created_at) VALUES ('" +
                escapeSql(newUser.getUsername()) + "', '" +
                escapeSql(newUser.getName()) + "', '" +
                escapeSql(newUser.getPassword()) + "', '" + // 이미 해싱된 비밀번호
                escapeSql(newUser.getEmail()) + "', '" +
                escapeSql(newUser.getPhone()) + "', " +
                newUser.getTotalPoint() + ", " +
                newUser.getAdminLevel() + ", " +
                "TO_DATE('" + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(newUser.getCreatedAt()) + "', 'YYYY-MM-DD HH24:MI:SS'))";

        entityManager.createNativeQuery(insertSql).executeUpdate();

        // 저장된 사용자의 ID를 가져오기 위한 다시 한번 SQL Injection 가능한 쿼리
        String selectAfterInsertSql = "SELECT user_id, username, name, password, email, phone, total_point, admin_level, created_at, updated_at FROM users WHERE username = '" + username + "'";

        List<Object[]> result = entityManager.createNativeQuery(selectAfterInsertSql).getResultList();
        if (result.isEmpty()) {
            throw new RuntimeException("저장된 사용자 정보를 찾을 수 없습니다.");
        }
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("저장된 사용자를 찾을 수 없습니다."));
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
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        });
        userRepository.findByPhone(request.getPhone()).ifPresent(user -> {
            throw new IllegalStateException("이미 존재하는 휴대폰 번호입니다.");
        });

        User newUser = User.builder()
                .username(request.getUsername())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .adminLevel(0)
                .totalPoint(0L)
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(newUser);
    }

    public boolean requestPasswordReset(String username, String email, String host) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getEmail().equals(email)) {
                String token = Base64.getEncoder().encodeToString(email.getBytes());
                String resetLink = "http://" + host + "/password-reset?token=" + token;

                String emailBody = "<html>"
                    + "<body>"
                    + "<h2>비밀번호 재설정 요청</h2>"
                    + "<p>비밀번호를 재설정하려면 아래 버튼을 클릭하세요.</p>"
                    + "<a href=\"" + resetLink + "\" style=\"background-color:#007bff;color:white;padding:10px 20px;text-align:center;text-decoration:none;display:inline-block;\">비밀번호 재설정</a>"
                    + "</body>"
                    + "</html>";

                mailService.sendMail(email, "비밀번호 재설정 링크입니다.", emailBody, true);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        String email;
        try {
            email = new String(Base64.getDecoder().decode(token));
        } catch (IllegalArgumentException e) {
            // Invalid Base64 token
            return false;
        }

        validatePassword(newPassword);

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.changePassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
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
        String sql = "SELECT user_id, username, name, password, email, phone, total_point, admin_level, created_at, updated_at FROM users WHERE user_id = " + userId;
        List<Object[]> result;
        try {
            result = entityManager.createNativeQuery(sql).getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error executing query: " + e.getMessage());
        }

        if (result.isEmpty()) {
            throw new UsernameNotFoundException("User not found with userId: " + userId);
        }

        Object[] userData = result.get(0);
        Map<String, Object> userInfo = new LinkedHashMap<>();
        // Note: The order and type of columns depends on the actual table schema
        userInfo.put("user_id", userData[0]);
        userInfo.put("username", userData[1]);
        userInfo.put("name", userData[2]);
        userInfo.put("password", userData[3]);
        userInfo.put("email", userData[4]);
        userInfo.put("phone", userData[5]);
        userInfo.put("total_point", userData[6]);
        userInfo.put("admin_lev", userData[7]);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (userData[8] != null) {
            Object rawCreatedAt = userData[8];
            LocalDateTime createdAt;
            if (rawCreatedAt instanceof java.sql.Timestamp) {
                createdAt = ((java.sql.Timestamp) rawCreatedAt).toLocalDateTime();
            } else if (rawCreatedAt instanceof Number) {
                createdAt = LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(((Number) rawCreatedAt).longValue()), java.time.ZoneId.systemDefault());
            } else {
                throw new RuntimeException("Cannot convert created_at of type " + rawCreatedAt.getClass().getName());
            }
            userInfo.put("created_at", createdAt.format(formatter));
        }

        if (userData[9] != null) {
            Object rawUpdatedAt = userData[9];
            LocalDateTime updatedAt;
            if (rawUpdatedAt instanceof java.sql.Timestamp) {
                updatedAt = ((java.sql.Timestamp) rawUpdatedAt).toLocalDateTime();
            } else if (rawUpdatedAt instanceof Number) {
                updatedAt = LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(((Number) rawUpdatedAt).longValue()), java.time.ZoneId.systemDefault());
            } else {
                updatedAt = null; // Or throw an exception, depending on expected behavior
            }

            if (updatedAt != null) {
                userInfo.put("updated_at", updatedAt.format(formatter));
            }
        } else {
            userInfo.put("updated_at", null);
        }

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

        validatePassword(newPassword);
        user.changePassword(passwordEncoder.encode(newPassword));
        
        return userRepository.save(user);
    }

    @Transactional
    public User updateUserInfo(String username, UpdateUserInfoRequest request, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            validateEmail(request.getEmail());
            userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
                throw new IllegalStateException("이미 사용중인 이메일입니다.");
            });
        }

        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            validatePhone(request.getPhone());
            userRepository.findByPhone(request.getPhone()).ifPresent(existingUser -> {
                throw new IllegalStateException("이미 사용중인 핸드폰번호입니다.");
            });
        }

        user.updateInfo(request.getName(), request.getEmail(), request.getPhone(), request.getAdmin_lev());

        return userRepository.save(user);
    }

    @Transactional
    public void saveRefreshToken(String username, String refreshToken) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        user.updateRefreshToken(refreshToken);
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
