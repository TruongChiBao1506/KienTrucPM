package iuh.fit.se.authservice.services.impl;

import iuh.fit.se.authservice.configs.JwtService;
import iuh.fit.se.authservice.dtos.AuthRequest;
import iuh.fit.se.authservice.dtos.AuthResponse;
import iuh.fit.se.authservice.dtos.RegisterRequest;
import iuh.fit.se.authservice.entities.RefreshToken;
import iuh.fit.se.authservice.entities.Role;
import iuh.fit.se.authservice.entities.User;
import iuh.fit.se.authservice.repository.UserRepository;
import iuh.fit.se.authservice.services.AuthService;
import iuh.fit.se.authservice.services.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService; // Thêm RefreshTokenService

    @Override
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Tạo Access Token
        String accessToken = jwtService.generateAccessToken(user);

        // Kiểm tra xem user đã có Refresh Token chưa
        Optional<RefreshToken> existingToken = refreshTokenService.findByUser(user);

        RefreshToken refreshToken;
        if (existingToken.isPresent()) {
            // Kiểm tra xem Refresh Token còn hạn không
            if (refreshTokenService.isExpired(existingToken.get())) {
                refreshTokenService.deleteByUser(user); // Xóa token cũ
                refreshToken = refreshTokenService.createRefreshToken(user.getUsername()); // Tạo mới
            } else {
                refreshToken = existingToken.get(); // Dùng lại token cũ nếu còn hạn
            }
        } else {
            refreshToken = refreshTokenService.createRefreshToken(user.getUsername()); // Tạo mới nếu chưa có
        }
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken()) // Trả về Refresh Token
                .role(user.getRole().name())
                .username(user.getUsername())
                .build();
    }

    @Override
    public Map<String, Object> register(RegisterRequest request) {
        Map<String, Object> response = new HashMap<>();

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", "Username already exists");
            return response;
        }

        // Tạo user mới
        User user = new User();
        user.setUsername(request.getUsername());
        user.setFullname(request.getFullname());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setDob(request.getDob());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setGender(request.isGender());
        user.setRole(request.getRole() != null ? request.getRole() : Role.USER);

        userRepository.save(user);

//        String accessToken = jwtService.generateAccessToken(user.getUsername());
//        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

        // ✅ Trả về thông tin user + token
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Đăng ký tài khoản thành công");
//        response.put("accessToken", accessToken);
//        response.put("refreshToken", refreshToken.getToken());
        response.put("username", user.getUsername());
        response.put("role", user.getRole().toString());

        return response;
    }
    @Override
    public AuthResponse refresh(String refreshTokenRequest) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenRequest)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        // Kiểm tra Refresh Token có hết hạn không
        if (refreshTokenService.isExpired(refreshToken)) {
            refreshTokenService.deleteByUser(refreshToken.getUser());
            throw new RuntimeException("Refresh token expired, please login again");
        }

        // Tạo Access Token mới
        String newAccessToken = jwtService.generateAccessToken(refreshToken.getUser());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken()) // Dùng lại Refresh Token cũ nếu còn hạn
                .build();
    }
    @Override
    public void logout(String refreshTokenRequest) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenRequest)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        refreshTokenService.deleteByUser(refreshToken.getUser()); // Xóa refresh token của user
    }
}
