package iuh.fit.se.userservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import iuh.fit.se.userservice.dtos.*;
import iuh.fit.se.userservice.entities.User;
import iuh.fit.se.userservice.events.dtos.UserEmailUpdateEvent;
import iuh.fit.se.userservice.events.listeners.UserEventListener;
import iuh.fit.se.userservice.events.publishers.UserEventPublisher;
import iuh.fit.se.userservice.feign.AuthServiceClient;
import iuh.fit.se.userservice.index.UserIndex;
import iuh.fit.se.userservice.repositories.UserRepository;
import iuh.fit.se.userservice.services.UserIndexService;
import iuh.fit.se.userservice.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
    private UserIndexService userIndexService;

    @Autowired
    private UserEventPublisher userEventPublisher;

    @Autowired
    private UserEventListener userEventListener;

    private final AuthServiceClient authServiceClient;
    @Autowired
    private ObjectMapper objectMapper;

    public UserController(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

//    @GetMapping("/hello")
//    public String hello() {
//        return "Hello from User Service";
//    }

    @GetMapping("/{username}")
    public ResponseEntity<Map<String, Object>> getUserInfo(@PathVariable String username) {
        Map<String, Object> response = new LinkedHashMap<>();
        UserProfileDTO profile = new UserProfileDTO();
        User user = userService.findByUsername(username).orElse(null);
        profile.setId(user.getId());
        profile.setUserId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setFullname(user.getFullname());
        profile.setPhone(user.getPhone());
        profile.setGender(user.isGender());
        profile.setDob(user.getDob());
        profile.setAddress(user.getAddress());
        ResponseEntity<Map<String, Object>> responseEmail = authServiceClient.getAuthUserEmailById(user.getUserId());
        if (responseEmail.getStatusCode() != HttpStatus.OK) {
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Lỗi khi lấy thông tin người dùng");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        String email = responseEmail.getBody().get("data").toString();
        profile.setEmail(email);
        response.put("status", HttpStatus.OK.value());
        response.put("data", profile);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/update-profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@Valid @RequestBody UserProfileDTO userProfileDTO, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            Map<String, Object> errors = new LinkedHashMap<String, Object>();
            bindingResult.getFieldErrors().stream().forEach(result -> {
                errors.put(result.getField(), result.getDefaultMessage());
            });
            Map<String, Object> response = new LinkedHashMap<String, Object>();
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("errors", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
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
            } else {
                User user = OPuser.get();
                user.setFullname(userProfileDTO.getFullname());

                user.setPhone(userProfileDTO.getPhone());
                user.setAddress(userProfileDTO.getAddress());
                user.setGender(userProfileDTO.isGender());
                user.setDob(userProfileDTO.getDob());
                userService.save(user);
                userIndexService.addUserToElasticsearch(user.getId());
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
            List<UserDTO> users = userService.findAll();
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
    public ResponseEntity<Map<String, Object>> getUserByRole(@PathVariable("userName") String userName) {
        User currentUser = userService.findByUsername(userName).get();
        System.out.println("currentUser: " + currentUser);
        Map<String, Object> response = new LinkedHashMap<>();
        List<UserDTO> users = null;
        String role = authServiceClient.getRoleByUserId(currentUser.getUserId());
        if (role.equals("SUPER")) {

            users = userService.findAll();
            System.out.println("users: " + users);
            response.put("status", HttpStatus.OK.value());
            response.put("data", users);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            List<Long> userIds = authServiceClient.getUserIdsByRole("USER");
            users = userService.findAllByIds(userIds);
            response.put("status", HttpStatus.OK.value());
            response.put("data", users);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        ResponseEntity<Map<String, Object>> authResponse = authServiceClient.deleteAuthUser(id);
        if (authResponse.getStatusCode() != HttpStatus.OK) {
            return authResponse;
        } else {
            userService.deleteById(id);
            userIndexService.deleteUserByUserId(id);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Xóa người dùng thành công");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addUser(@RequestBody @Valid UserDTO userDTO, BindingResult bindingResult) {
        try{
            Map<String, Object> response = new LinkedHashMap<>();
            if (bindingResult.hasErrors()) {
                Map<String, Object> errors = new LinkedHashMap<String, Object>();

                bindingResult.getFieldErrors().stream().forEach(result -> {
                    errors.put(result.getField(), result.getDefaultMessage());
                });
                response.put("status", HttpStatus.BAD_REQUEST.value());
                response.put("message", "Invalid data");
                response.put("errors", errors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            } else {
                AuthUser authUser = new AuthUser();
                authUser.setUsername(userDTO.getUsername());
                authUser.setPassword(userDTO.getPassword());
                authUser.setEmail(userDTO.getEmail());
                authUser.setRole(userDTO.getRole());
                ResponseEntity<Map<String, Object>> authResponse = authServiceClient.addAuthUser(authUser);
                if (authResponse.getStatusCode() != HttpStatus.OK) {
                    return authResponse;
                } else {
                    try {
                        User user = new User();
                        user.setUsername(userDTO.getUsername());
                        user.setFullname(userDTO.getFullname());
                        user.setPhone(userDTO.getPhone());
                        user.setAddress(userDTO.getAddress());
                        user.setGender(userDTO.isGender());
                        user.setDob(userDTO.getDob());
                        user.setUserId(Long.valueOf(authResponse.getBody().get("message").toString()));
                        userService.save(user);
                        userIndexService.addUserToElasticsearch(user.getUserId());
                        response.put("status", HttpStatus.OK.value());
                        response.put("message", "Thêm người dùng thành công");
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                    } catch (Exception e) {
                        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
                        response.put("message", "Lỗi khi thêm người dùng: " + e.getMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                }
            }
        }
        catch (Exception e){
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Error adding user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>>updateUser(@PathVariable("id") Long id, @Valid @RequestBody UserUpdateDTO userDTO, BindingResult bindingResult) {
        try {
            Map<String, Object> response = new LinkedHashMap<>();
            if (bindingResult.hasErrors()) {
                Map<String, Object> errors = new LinkedHashMap<String, Object>();

                bindingResult.getFieldErrors().stream().forEach(result -> {
                    errors.put(result.getField(), result.getDefaultMessage());
                });
                System.out.println("errors: " + errors);
                response.put("status", HttpStatus.BAD_REQUEST.value());
                response.put("message", "Invalid data");
                response.put("errors", errors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            else{
                User user = userService.findById(id);
                if(user == null){
                    response.put("status", HttpStatus.NOT_FOUND.value());
                    response.put("message", "User not found");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                else{
                    AuthUser authUser = new AuthUser();
                    authUser.setId(user.getUserId());
                    authUser.setUsername(userDTO.getUsername());
                    authUser.setEmail(userDTO.getEmail());
//                    authUser.setPassword(userDTO.getPassword());
                    ResponseEntity<Map<String, Object>> authResponse = authServiceClient.updateAuthUser(authUser);
                    if(authResponse.getStatusCode() != HttpStatus.OK){
                        return authResponse;
                    }
                    else{
                        user.setUsername(userDTO.getUsername());
                        user.setFullname(userDTO.getFullname());
                        user.setPhone(userDTO.getPhone());
                        user.setAddress(userDTO.getAddress());
                        user.setGender(userDTO.isGender());
                        user.setDob(userDTO.getDob());
                        userService.save(user);
                        userIndexService.addUserToElasticsearch(user.getUserId());
                        response.put("status", HttpStatus.OK.value());
                        response.put("message", "Cập nhật thông tin thành công");
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                    }
                }
            }
        }
        catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/user/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable("id") Long id){
        try{
            User user = userService.findByUserId(id);
            if(user == null){
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("status", HttpStatus.NOT_FOUND.value());
                response.put("message", "User not found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            else{
                ResponseEntity<Map<String, Object>> authResponse = authServiceClient.getAuthUser(id);
                if(authResponse.getStatusCode() != HttpStatus.OK){
                    return authResponse;
                }
                else{
                    System.out.println(authResponse.getBody().get("status"));
                    UserDTO userDTO = new UserDTO();
                    Map<String, Object> responseBody = authResponse.getBody();
                    AuthUser authUser = objectMapper.convertValue(responseBody.get("data"), AuthUser.class);
                    userDTO.setUsername(authUser.getUsername());
                    userDTO.setPassword(authUser.getPassword());
                    userDTO.setEmail(authUser.getEmail());
                    userDTO.setRole(authUser.getRole());
                    userDTO.setFullname(user.getFullname());
                    userDTO.setPhone(user.getPhone());
                    userDTO.setAddress(user.getAddress());
                    userDTO.setGender(user.isGender());
                    userDTO.setDob(user.getDob());
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("status", HttpStatus.OK.value());
                    response.put("message", "Lấy thông tin người dùng thành công");
                    response.put("data", userDTO);
                    return ResponseEntity.ok(response);
                }
            }
        }
        catch (Exception e){
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Lỗi khi lấy thông tin người dùng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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
    @GetMapping("/filter")
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(value = "keyword",required = false) String keyword,
            @RequestParam(value = "gender",required = false) String gender,
            @RequestParam(value = "role",required = false) String role
    ) {
        System.out.println("keyword: " + keyword);
        System.out.println("gender: " + gender);
        System.out.println("role: " + role);
        List<UserDTO> users = userService.filterUsers(keyword, gender, role);
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", users);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncUsers(){
        try{
            Map<String, Object> response = new LinkedHashMap<String, Object>();
            userIndexService.syncUsersToElasticsearch();
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Đồng bộ người dùng thành công");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }catch (Exception e){
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Lỗi khi đồng bộ người dùng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/search")
    public List<UserIndex> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String role) throws IOException {
        return userService.searchUsers(keyword, gender, role);
    }
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> handleChangePassword(@RequestBody AuthUserChangePassword authUserChangePassword) {
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            ResponseEntity<Map<String, Object>> authResponse = authServiceClient.changePassword(authUserChangePassword);
            return ResponseEntity.ok(authResponse.getBody()); // Trả về phản hồi từ auth-service nếu thành công
        } catch (FeignException.BadRequest e) {
            // Lấy nội dung phản hồi từ auth-service
            String responseBody = e.contentUTF8();
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", responseBody.contains("Mật khẩu cũ không đúng") ? "Mật khẩu cũ không đúng" : "Yêu cầu không hợp lệ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (FeignException.NotFound e) {
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "Người dùng không tồn tại");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (FeignException e) {
            // Xử lý các lỗi khác từ auth-service
            response.put("status", e.status());
            response.put("message", "Lỗi từ AuthService: " + e.getMessage());
            return ResponseEntity.status(e.status()).body(response);
        } catch (Exception e) {
            // Xử lý lỗi hệ thống
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Lỗi hệ thống, vui lòng thử lại sau!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/user-profile/{id}")
    public ResponseEntity<Map<String, Object>> getUserProfileById (@PathVariable Long id){
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            response.put("status", HttpStatus.OK.value());
            response.put("data", userService.findById(id));
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Lỗi khi lấy thông tin người dùng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
