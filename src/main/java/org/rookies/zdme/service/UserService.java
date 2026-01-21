package org.rookies.zdme.service;

import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

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
}
