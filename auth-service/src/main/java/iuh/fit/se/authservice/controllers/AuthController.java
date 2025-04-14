package iuh.fit.se.authservice.controllers;

import iuh.fit.se.authservice.configs.JwtService;
import iuh.fit.se.authservice.dtos.*;
import iuh.fit.se.authservice.entities.RefreshToken;
import iuh.fit.se.authservice.entities.Role;
import iuh.fit.se.authservice.entities.User;
import iuh.fit.se.authservice.services.AuthService;
import iuh.fit.se.authservice.services.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Xử lý đăng ký người dùng
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @Valid @RequestBody RegisterRequest request,
            BindingResult result) {

        // Kiểm tra lỗi validation đầu vào
        if (result.hasErrors()) {
            Map<String, Object> errors = new LinkedHashMap<>();
            result.getFieldErrors().forEach(error -> {
                errors.put(error.getField(), error.getDefaultMessage());
            });

            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("errors", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Gọi AuthService để xử lý đăng ký
        Map<String, Object> response = authService.register(request);
        HttpStatus status = (Integer) response.get("status") == HttpStatus.OK.value()
                ? HttpStatus.OK
                : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(response);
    }

    /**
     * Xử lý đăng nhập người dùng
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AuthRequest request) {
        try {
            AuthResponse authResponse = authService.login(request);

            // Tạo refreshToken dưới dạng Cookie
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", authResponse.getRefreshToken())
                    .httpOnly(true)
                    .secure(false) // true nếu dùng HTTPS
                    .path("/")
                    .maxAge(Duration.ofDays(7))
                    .sameSite("Strict")
                    .build();

            // Tạo body response (không trả refreshToken)
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", HttpStatus.OK.value());

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("accessToken", authResponse.getAccessToken());
            data.put("username", authResponse.getUsername());
            data.put("role", authResponse.getRole());
            response.put("data", data);

            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(response);

        } catch (BadCredentialsException e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            response.put("message", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        System.out.println("📥 Nhận refreshToken từ gateway: " + request.getRefreshToken());
        Optional<RefreshToken> refreshTokenOptional = refreshTokenService.findByToken(request.getRefreshToken());
        if (refreshTokenOptional.isEmpty()) {
            System.out.println("❌ Refresh token không hợp lệ");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        RefreshToken refreshToken = refreshTokenOptional.get();
        if(!refreshTokenService.verifyExpiration(refreshToken)) {
            System.out.println("❌ Refresh token đã hết hạn");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody RefreshRequest request) {
        System.out.println(request.getRefreshToken());
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok("Logout successful");
    }

    @GetMapping("/all-by-role/{role}")
    public ResponseEntity<List<Long>> getUserIdsByRole(@PathVariable("role") String role){
        List<Long> userIds = authService.getUserIdsByRole(role);
        return ResponseEntity.ok(userIds);
    }
    @GetMapping("/role/{userId}")
    public ResponseEntity<String> getRoleByUserId(@PathVariable("userId") Long userId){
        String role = authService.getRoleByUserId(userId);
        return ResponseEntity.ok(role);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteAuthUser(@PathVariable("id") Long id){
        authService.deleteAuthUser(id);
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Xóa người dùng thành công");
        return ResponseEntity.ok(response);
    }
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addAuthUser(@RequestBody AuthUser authUser){
        Map<String, Object> response = new HashMap<>();
        try{
            User user = new User();
            user.setId(authUser.getId());
            user.setUsername(authUser.getUsername());
            user.setPassword(passwordEncoder.encode(authUser.getPassword()));
            user.setEmail(authUser.getEmail());
            try{
                user.setRole(Role.valueOf(authUser.getRole()));
            }catch (IllegalArgumentException e){
                response.put("status", HttpStatus.BAD_REQUEST.value());
                response.put("message", "Role không hợp lệ: " + authUser.getRole());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            authService.save(user);
            response.put("status", HttpStatus.OK.value());
            response.put("message", user.getId());
            return ResponseEntity.ok(response);
        }
        catch (Exception e){
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Lỗi khi thêm người dùng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateAuthUser(@RequestBody AuthUser authUser){
        Map<String, Object> response = new HashMap<>();
        try{
            User user = authService.findById(authUser.getId());
            if(user == null){
                response.put("status", HttpStatus.NOT_FOUND.value());
                response.put("message", "Không tìm thấy người dùng với ID: " + authUser.getId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            else {
                user.setUsername(authUser.getUsername());
                if (authUser.getPassword() != null && !authUser.getPassword().isEmpty() && !authUser.getPassword().isBlank() && !authUser.getPassword().equals(user.getPassword())) {
                    user.setPassword(passwordEncoder.encode(authUser.getPassword()));
                }
                user.setEmail(authUser.getEmail());
                authService.save(user);
                response.put("status", HttpStatus.OK.value());
                response.put("message", "Cập nhật người dùng thành công");
                return ResponseEntity.ok(response);
            }
        }
        catch (Exception e){
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Lỗi khi cập nhật người dùng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/user/{id}")
    public  ResponseEntity<Map<String, Object>> getAuthUserById(@PathVariable("id") Long id){
        try{
            User user = authService.findById(id);
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", HttpStatus.NOT_FOUND.value());
                response.put("message", "Không tìm thấy người dùng với ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            else {
                System.out.println(user.toString());
                Map<String, Object> response = new HashMap<>();
                response.put("status", HttpStatus.OK.value());
                response.put("data", user);
                return ResponseEntity.ok(response);
            }
        }
        catch (Exception e){
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Lỗi khi lấy thông tin người dùng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/user-email/{id}")
    public  ResponseEntity<Map<String, Object>> getAuthUserEmailById(@PathVariable("id") Long id){
        try{
            User user = authService.findById(id);
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", HttpStatus.NOT_FOUND.value());
                response.put("message", "Không tìm thấy người dùng với ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            else {
                System.out.println(user.toString());
                Map<String, Object> response = new HashMap<>();
                response.put("status", HttpStatus.OK.value());
                response.put("data", user.getEmail());
                return ResponseEntity.ok(response);
            }
        }
        catch (Exception e){
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Lỗi khi lấy thông tin người dùng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody AuthUserChangePassword authUserChangePassword){
        Map<String, Object> response = new LinkedHashMap<>();
        try{
            authService.changePassword(authUserChangePassword);
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Password changed successfully");
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) { // Bắt lỗi cụ thể hơn
            response.put("status", e.getStatusCode().value());
            response.put("message", e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(response);
        } catch (Exception e) {
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value()); // Tránh lỗi 500 không mong muốn
            response.put("message", "Lỗi hệ thống, vui lòng thử lại sau!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/get-refresh-token")
    public ResponseEntity<Map<String, Object>>getRefreshToken(@RequestParam String username){
        try{
            User user = authService.findByUsername(username);
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", HttpStatus.NOT_FOUND.value());
                response.put("message", "Không tìm thấy người dùng với username: " + username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            Optional<RefreshToken> refreshTokenOptional = refreshTokenService.findByUser(user);
            if (refreshTokenOptional.isPresent()) {
                RefreshToken refreshToken = refreshTokenOptional.get();
                if (refreshTokenService.verifyExpiration(refreshToken)) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", HttpStatus.OK.value());
                    response.put("refreshToken", refreshToken.getToken());
                    return ResponseEntity.ok(response);
                } else {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", HttpStatus.UNAUTHORIZED.value());
                    response.put("message", "Refresh token đã hết hạn");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("status", HttpStatus.NOT_FOUND.value());
                response.put("message", "Không tìm thấy refresh token cho người dùng: " + username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        }catch (Exception e){
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Lỗi khi lấy refresh token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }


    }
}
