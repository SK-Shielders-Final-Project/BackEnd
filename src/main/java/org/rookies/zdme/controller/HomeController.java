package org.rookies.zdme.controller;

import org.rookies.zdme.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @Autowired
    private UserService userService;

    @GetMapping("/api/user/info/{user_id}")
    public ResponseEntity<Map<String, Object>> getUserInfo(@PathVariable("user_id") Long userId) {
        try {
            Map<String, Object> userInfo = userService.getUserInfo(userId);
            return new ResponseEntity<>(userInfo, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}
