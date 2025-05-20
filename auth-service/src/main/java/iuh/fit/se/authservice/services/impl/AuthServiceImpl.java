package iuh.fit.se.authservice.services.impl;

import iuh.fit.se.authservice.configs.JwtService;
import iuh.fit.se.authservice.dtos.*;
import iuh.fit.se.authservice.events.publishers.UserEventPublisher;
import iuh.fit.se.authservice.events.dtos.UserProfileCreatedEvent;
import iuh.fit.se.authservice.entities.RefreshToken;
import iuh.fit.se.authservice.entities.Role;
import iuh.fit.se.authservice.entities.User;
import iuh.fit.se.authservice.repository.RefreshTokenRepository;
import iuh.fit.se.authservice.repository.UserRepository;
import iuh.fit.se.authservice.services.AuthService;
import iuh.fit.se.authservice.services.OtpService;
import iuh.fit.se.authservice.services.RefreshTokenService;
import iuh.fit.se.authservice.utils.OtpGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

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

    @Autowired
    private UserEventPublisher userEventPublisher;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private OtpGenerator otpGenerator = new OtpGenerator();

    @Autowired
    private OtpService otpService;

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
                .userId(user.getId())
                .build();
    }

    @Override
    public Map<String, Object> register(RegisterRequest request) {
        Map<String, Object> response = new HashMap<>();
        // 1. Kiểm tra username đã tồn tại chưa
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", "Username already exists");
            return response;
        }
        // 2. Kiểm tra rate limit
        if (!otpService.isAllowedToSendOtp(request.getEmail())) {
            response.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
            response.put("message", "Bạn đã gửi OTP quá nhiều lần. Vui lòng thử lại sau 1 phút.");
            return response;
        }
        //3. Tạo OTP
        String otp = OtpGenerator.generateOtp();
        otpService.saveRequestAndOtp(request, otp);
        //4. Gửi OTP qua email
        OtpEmailEvent otpEmailEvent = new OtpEmailEvent(request.getEmail(), otp);
        userEventPublisher.publishSendOtpEamil(otpEmailEvent);
        // 5. Trả phản hồi
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Đăng ký tài khoản thành công. Vui lòng kiểm tra email để xác thực.");
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
    @Transactional
    public void logout(String refreshTokenRequest) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenRequest)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        refreshTokenService.deleteByUser(refreshToken.getUser()); // Xóa refresh token của user
    }

    @Override
    public List<Long> getUserIdsByRole(String role) {
        return userRepository.findByRole(Role.valueOf(role)).stream().map(User::getId).toList();
    }

    @Override
    public String getRoleByUserId(Long userId) {
        return userRepository.findById(userId).map(user -> user.getRole().name()).orElse(null);
    }
    @Override
    @Transactional
    public void deleteAuthUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy người dùng với ID: " + id);
        }
//        if (userRepository.findById(id).get().getOrders().size() > 0) {
//            throw new RuntimeException("Không thể xóa người dùng đã đặt hàng");
//        }
        refreshTokenRepository.deleteByUser(userRepository.findById(id).get());
        userRepository.deleteById(id);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public void changePassword(AuthUserChangePassword authUserChangePassword) {
        Optional<User> user = userRepository.findByUsername(authUserChangePassword.getUsername());
        if (user.isPresent()) {
            if (passwordEncoder.matches(authUserChangePassword.getPassword(), user.get().getPassword())) {
                user.get().setPassword(passwordEncoder.encode(authUserChangePassword.getNewPassword()));
                userRepository.save(user.get());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu cũ không đúng");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng");
        }
    }

    @Override
    public void updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
