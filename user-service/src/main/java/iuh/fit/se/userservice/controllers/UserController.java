package iuh.fit.se.userservice.controllers;

import iuh.fit.se.userservice.dtos.UserProfileDTO;
import iuh.fit.se.userservice.entities.User;
import iuh.fit.se.userservice.events.dtos.UserEmailUpdateEvent;
import iuh.fit.se.userservice.events.listeners.UserEventListener;
import iuh.fit.se.userservice.events.publishers.UserEventPublisher;
import iuh.fit.se.userservice.feign.AuthServiceClient;
import iuh.fit.se.userservice.repositories.UserRepository;
import iuh.fit.se.userservice.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserEventPublisher userEventPublisher;

    @Autowired
    private UserEventListener userEventListener;


    private final AuthServiceClient authServiceClient;

    public UserController(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from User Service";
    }

    @GetMapping("/{username}")
    public ResponseEntity<Map<String, Object>> getUserInfo(@PathVariable String username) {
        System.out.println("username: " + username);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", userService.findByUsername(username));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @PostMapping("/update-profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@Valid @RequestBody UserProfileDTO userProfileDTO, BindingResult bindingResult) {

        if(bindingResult.hasErrors()) {
            Map<String, Object> errors = new LinkedHashMap<String, Object>();
            bindingResult.getFieldErrors().stream().forEach(result -> {
                errors.put(result.getField(), result.getDefaultMessage());
            });
            Map<String, Object> response = new LinkedHashMap<String, Object>();
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("errors", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }else {
            Optional<User> OPuser = userService.findByUsername(userProfileDTO.getUsername());
            if (OPuser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
            }
            UserEmailUpdateEvent userEmailUpdateEvent = new UserEmailUpdateEvent();
            userEmailUpdateEvent.setUserId(OPuser.get().getId());
            userEmailUpdateEvent.setEmail(userProfileDTO.getEmail());
            boolean isUpdated = updateEmail(userEmailUpdateEvent);
            if (!isUpdated) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Cập nhật thông tin thất bại!"));
            }
            else{
                User user = OPuser.get();
                user.setFullname(userProfileDTO.getFullname());

                user.setPhone(userProfileDTO.getPhone());
                user.setAddress(userProfileDTO.getAddress());
                user.setGender(userProfileDTO.isGender());
                user.setDob(userProfileDTO.getDob());
                userService.save(user);

                Map<String, Object> response = new LinkedHashMap<String, Object>();
                response.put("status", HttpStatus.OK.value());
                response.put("message", "Cập nhật thông tin thành công!");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
        }
    }
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        try {
            List<User> users = userService.findAll();
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", HttpStatus.OK.value());
            response.put("data", users);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Lỗi khi lấy danh sách người dùng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/all-by-role/{userName}")
    public ResponseEntity<Map<String, Object>> getUserByRole(@PathVariable("userName") String userName){
        User currentUser  = userService.findByUsername(userName).get();
        System.out.println("currentUser: " + currentUser);
        Map<String, Object> response = new LinkedHashMap<>();
        List<User> users = null ;
        String role = authServiceClient.getRoleByUserId(currentUser.getUserId());
        if(role.equals("SUPER")){
            users = userService.findAll();
            response.put("status", HttpStatus.OK.value());
            response.put("data", users);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        else{
            List<Long> userIds = authServiceClient.getUserIdsByRole("USER");
            users = userService.findAllByIds(userIds);
            response.put("status", HttpStatus.OK.value());
            response.put("data", users);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    public boolean updateEmail(UserEmailUpdateEvent userEmailUpdateEvent) {
        // Gửi sự kiện yêu cầu cập nhật email
        userEventPublisher.publishUserEmailUpdate(userEmailUpdateEvent);

        // Tạo CompletableFuture để chờ phản hồi
        CompletableFuture<Boolean> future = userEventListener.waitForEmailUpdate(userEmailUpdateEvent.getUserId());

        try {
            // Đợi tối đa 10 giây để nhận phản hồi từ Kafka
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Timeout hoặc lỗi
        }
    }
}
