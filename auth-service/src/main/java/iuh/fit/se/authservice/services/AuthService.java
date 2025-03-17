package iuh.fit.se.authservice.services;

import iuh.fit.se.authservice.dtos.AuthRequest;
import iuh.fit.se.authservice.dtos.AuthResponse;
import iuh.fit.se.authservice.dtos.RegisterRequest;

import java.util.List;
import java.util.Map;

public interface AuthService {
    public AuthResponse login(AuthRequest request);
    public Map<String, Object> register(RegisterRequest request);
    public AuthResponse refresh(String refreshTokenRequest);
    public void logout(String refreshTokenRequest);
    public List<Long> getUserIdsByRole(String role);
    public String getRoleByUserId(Long userId);
}
