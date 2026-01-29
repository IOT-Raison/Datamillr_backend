package com.example.modbusapplication.Controller;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.modbusapplication.Model.UpdateUserDAO;
import com.example.modbusapplication.Service.LoginService;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final LoginService loginService;
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
  
    }

    @PostMapping("/init")
    public ResponseEntity<Map<String, String>> sendRandomNumber(HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        ipAddress = normalizeIp(ipAddress);
        // System.out.println("Received IP Address: " + ipAddress);

        String randomNumber = loginService.storeLoginInfo(ipAddress);
        return ResponseEntity.ok(Map.of("randomNumber", randomNumber));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {

        String hashedCredential = requestBody.get("hashedCredential");
        String ip = normalizeIp(getClientIp(request));

        return loginService.loginResponse(hashedCredential, ip);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim(); // In case of multiple IPs
        }

        ip = request.getHeader("X-Real-IP"); // some proxies use this
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        return request.getRemoteAddr();
    }

    private String normalizeIp(String ip) {
        if (ip == null)
            return "UNKNOWN";
        if ("::1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }

    @PostMapping("/update-credentials")
    public ResponseEntity<?> updateCredentials(@RequestBody UpdateUserDAO updateUserDAO) {
        if (updateUserDAO.getOldUserId() == null || updateUserDAO.getOldPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Old userId and password are required"));
        }

        return loginService.updateUserIdPassword(updateUserDAO);
    }

}
