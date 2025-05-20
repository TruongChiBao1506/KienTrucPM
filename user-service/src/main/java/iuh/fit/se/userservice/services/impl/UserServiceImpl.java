package iuh.fit.se.userservice.services.impl;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import iuh.fit.se.userservice.dtos.UserDTO;
import iuh.fit.se.userservice.entities.User;
import iuh.fit.se.userservice.feign.AuthServiceClient;
//import iuh.fit.se.userservice.index.UserIndex;
import iuh.fit.se.userservice.repositories.UserRepository;
import iuh.fit.se.userservice.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthServiceClient authServiceClient;

//    @Autowired
//    private ElasticsearchClient elasticsearchClient;

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<UserDTO> findAll() {

        List<User> userList = userRepository.findAll();
        List<UserDTO> userDTOS = new ArrayList<>();

        for (User user : userList) {
            UserDTO userDTO = new UserDTO();

            // Lấy email từ auth service
            ResponseEntity<Map<String, Object>> responseEmail = authServiceClient.getAuthUserEmailById(user.getUserId());
            String email = responseEmail.getBody().get("data").toString();

            // Lấy vai trò
            String roleUser = authServiceClient.getRoleByUserId(user.getUserId());

            // Gán dữ liệu vào DTO
            userDTO.setEmail(email);
            userDTO.setId(user.getId());
            userDTO.setFullname(user.getFullname());
            userDTO.setUsername(user.getUsername());
            userDTO.setPhone(user.getPhone());
            userDTO.setAddress(user.getAddress());
            userDTO.setGender(user.isGender());
            userDTO.setDob(user.getDob());
            userDTO.setRole(roleUser);

            userDTOS.add(userDTO);
        }

        return userDTOS;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy người dùng với ID: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        User userInDB = userRepository.findById(user.getId()).orElseThrow(() -> new RuntimeException("User not found"));

        if (userInDB == null) {
            return null;
        } else {
            userInDB.setFullname(user.getFullname());
            userInDB.setPhone(user.getPhone());
            userInDB.setAddress(user.getAddress());
            userInDB.setGender(user.isGender());
            userInDB.setDob(user.getDob());
            userInDB.setUsername(user.getUsername());
            userRepository.save(userInDB);
            return userInDB;
        }
    }

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "filterUsersFallback")
    public List<UserDTO> filterUsers(String keyword, String gender, String role) {
        Specification<User> spec = Specification.where(null);
        // Thêm điều kiện tìm kiếm theo từ khóa
        if (keyword != null && !keyword.isEmpty()) {
            logger.debug("Filtering by keyword: {}", keyword);
            spec = spec.and(containsKeyword(keyword));
        }
        // Thêm điều kiện lọc theo giới tính
        if (gender != null && !gender.isEmpty()) {
            logger.debug("Filtering by gender: {}", gender);
            spec = spec.and(hasGender(gender.equals("Male")));
        }
        if (role != null && !role.isEmpty()) {
            logger.debug("Filtering by role: {}", role);
            List<Long> userIds = authServiceClient.getUserIdsByRole(role);
            if (userIds.size() > 0) {
                spec = spec.and(hasUserIdIn(userIds));
            }
        }

        List<User> userList =  userRepository.findAll(spec);
        List<UserDTO> userDTOS = new ArrayList<>();
        for (User user : userList) {
            UserDTO userDTO = new UserDTO();
            ResponseEntity<Map<String, Object>> responseEmail = authServiceClient.getAuthUserEmailById(user.getUserId());
            String email = responseEmail.getBody().get("data").toString();
            System.out.println("Email: " + email);
            userDTO.setEmail(email);
            userDTO.setId(user.getId());
            userDTO.setFullname(user.getFullname());
            userDTO.setUsername(user.getUsername());
            userDTO.setPhone(user.getPhone());
            userDTO.setAddress(user.getAddress());
            userDTO.setGender(user.isGender());

            userDTO.setDob(user.getDob());
            String roleUser = authServiceClient.getRoleByUserId(user.getUserId());
            userDTO.setRole(roleUser);
            userDTOS.add(userDTO);
        }
        return userDTOS;
    }

    // Fallback method for filterUsers
    public List<User> filterUsersFallback(String keyword, String gender, String role, Throwable t) {
        logger.error("Circuit breaker triggered for filterUsers. Error: {}", t.getMessage(), t);

        // Nếu lỗi trong việc lấy role từ auth-service, vẫn có thể lọc theo keyword và gender
        Specification<User> spec = Specification.where(null);

        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and(containsKeyword(keyword));
        }

        if (gender != null && !gender.isEmpty()) {
            spec = spec.and(hasGender(gender.equals("Male")));
        }

        // Trả về dữ liệu chỉ lọc theo các điều kiện cục bộ
        try {
            logger.info("Applying fallback strategy: filtering locally by keyword and gender");
            return userRepository.findAll(spec);
        } catch (Exception ex) {
            logger.error("Error in fallback method: {}", ex.getMessage(), ex);
            return new ArrayList<>(); // Trả về danh sách rỗng nếu cả fallback cũng thất bại
        }
    }

    @Override
    public User findByUserId(Long userId) {
        return userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public List<UserDTO> findAllByIds(List<Long> ids) {

        List<User> userList = userRepository.findAllById(ids);
        List<UserDTO> userDTOS = new ArrayList<>();

        for (User user : userList) {
            UserDTO userDTO = new UserDTO();

            // Lấy email từ auth service
            ResponseEntity<Map<String, Object>> responseEmail = authServiceClient.getAuthUserEmailById(user.getUserId());
            String email = responseEmail.getBody().get("data").toString();

            // Lấy vai trò
            String roleUser = authServiceClient.getRoleByUserId(user.getUserId());

            // Gán dữ liệu vào DTO
            userDTO.setEmail(email);
            userDTO.setId(user.getId());
            userDTO.setFullname(user.getFullname());
            userDTO.setUsername(user.getUsername());
            userDTO.setPhone(user.getPhone());
            userDTO.setAddress(user.getAddress());
            userDTO.setGender(user.isGender());
            userDTO.setDob(user.getDob());
            userDTO.setRole(roleUser);

            userDTOS.add(userDTO);
        }

        return userDTOS;
    }

//    @Override
//    @CircuitBreaker(name = "userService", fallbackMethod = "searchUsersFallback")
//    public List<UserIndex> searchUsers(String keyword, String gender, String role) throws IOException {
//        logger.debug("Searching users - keyword: {}, gender: {}, role: {}", keyword, gender, role);
//        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
//
//        // Tìm kiếm theo keyword (fullname, email, phone, username, address)
//        if (keyword != null && !keyword.isEmpty()) {
//            boolQuery.should(MatchQuery.of(m -> m.field("fullname").query(keyword))._toQuery());
//            boolQuery.should(MatchQuery.of(m -> m.field("email").query(keyword))._toQuery());
//            boolQuery.should(MatchQuery.of(m -> m.field("phone").query(keyword))._toQuery());
//            boolQuery.should(MatchQuery.of(m -> m.field("username").query(keyword))._toQuery());
//            boolQuery.should(MatchQuery.of(m -> m.field("address").query(keyword))._toQuery());
//        }
//
//        // Lọc theo giới tính
//        if (gender != null) {
//            boolQuery.filter(TermQuery.of(t -> t.field("gender").value(gender))._toQuery());
//        }
//
//        // Lọc theo role
//        if (role != null && !role.isEmpty()) {
//            boolQuery.filter(TermQuery.of(t -> t.field("role").value(role))._toQuery());
//        }
//
//        // Thực hiện truy vấn
//        SearchResponse<UserIndex> response = elasticsearchClient.search(s -> s
//                        .index("users")
//                        .query(boolQuery.build()._toQuery()),
//                UserIndex.class);
//
//        // Lấy kết quả từ Elasticsearch
//        return response.hits().hits().stream()
//                .map(Hit::source)
//                .collect(Collectors.toList());
//    }
//
//    // Fallback method for searchUsers
//    public List<User> searchUsersFallback(String keyword, String gender, String role, Throwable t) {
//        logger.error("Circuit breaker triggered for searchUsers. Error: {}", t.getMessage(), t);
//
//        try {
//            logger.info("Applying fallback strategy: converting database results to UserIndex");
//
//            // Thay vì trả về danh sách rỗng, thử lấy dữ liệu từ database và chuyển đổi thành UserIndex
//            Specification<User> spec = Specification.where(null);
//
//            if (keyword != null && !keyword.isEmpty()) {
//                spec = spec.and(containsKeyword(keyword));
//            }
//
//            if (gender != null && !gender.isEmpty()) {
//                spec = spec.and(hasGender(gender.equals("Male")));
//            }
//
//            // Lấy danh sách user từ database
//            List<User> users = userRepository.findAll(spec);
//
//            // Chuyển đổi List<User> sang List<UserIndex>
//            return users.stream()
//                    .map(user -> {
//                        User userObj = new User();
//                        userObj.setUserId(user.getId());
//                        userObj.setFullname(user.getFullname());
//                        userObj.setUsername(user.getUsername());
//                        userObj.setPhone(user.getPhone());
//                        userObj.setAddress(user.getAddress());
//                        userObj.setGender(user.isGender());
//                        // Role không có sẵn trong User entity, có thể để trống hoặc mặc định
//                        return userObj;
//                    })
//                    .collect(Collectors.toList());
//
//        } catch (Exception ex) {
//            logger.error("Error in fallback method: {}", ex.getMessage(), ex);
//            return new ArrayList<>(); // Trả về danh sách rỗng chỉ khi fallback cũng thất bại
//        }
//    }
    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "findAllPaginatedFallback")
    public Page<UserDTO> findAllPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(pageable);
        
        return userPage.map(user -> {
            UserDTO userDTO = new UserDTO();
            
            // Lấy email từ auth service
            ResponseEntity<Map<String, Object>> responseEmail = authServiceClient.getAuthUserEmailById(user.getUserId());
            String email = responseEmail.getBody().get("data").toString();
            
            // Lấy vai trò
            String roleUser = authServiceClient.getRoleByUserId(user.getUserId());
            
            // Gán dữ liệu vào DTO
            userDTO.setEmail(email);
            userDTO.setId(user.getId());
            userDTO.setFullname(user.getFullname());
            userDTO.setUsername(user.getUsername());
            userDTO.setPhone(user.getPhone());
            userDTO.setAddress(user.getAddress());
            userDTO.setGender(user.isGender());
            userDTO.setDob(user.getDob());
            userDTO.setRole(roleUser);
            
            return userDTO;
        });
    }
      // Fallback method for findAllPaginated
    public Page<UserDTO> findAllPaginatedFallback(int page, int size, Throwable t) {
        logger.error("Circuit breaker triggered for findAllPaginated. Error: {}", t.getMessage(), t);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> userPage = userRepository.findAll(pageable);
            
            // Chuyển đổi sang DTO nhưng bỏ qua thông tin từ auth-service
            return userPage.map(user -> {
                UserDTO userDTO = new UserDTO();
                userDTO.setEmail(""); // Không có thông tin email
                userDTO.setId(user.getId());
                userDTO.setFullname(user.getFullname());
                userDTO.setUsername(user.getUsername());
                userDTO.setPhone(user.getPhone());
                userDTO.setAddress(user.getAddress());
                userDTO.setGender(user.isGender());
                userDTO.setDob(user.getDob());
                userDTO.setRole(""); // Không có thông tin vai trò
                
                return userDTO;
            });
        } catch (Exception ex) {
            logger.error("Error in fallback method: {}", ex.getMessage(), ex);
            // Trả về Page rỗng
            return new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), 0);
        }
    }
    
    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "findAllByIdsPaginatedFallback")
    public Page<UserDTO> findAllByIdsPaginated(List<Long> ids, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        // Tạo Specification để lọc theo danh sách ID
        Specification<User> spec = hasUserIdIn(ids);
        Page<User> userPage = userRepository.findAll(spec, pageable);
        
        return userPage.map(user -> {
            UserDTO userDTO = new UserDTO();
            
            // Lấy email từ auth service
            ResponseEntity<Map<String, Object>> responseEmail = authServiceClient.getAuthUserEmailById(user.getUserId());
            String email = responseEmail.getBody().get("data").toString();
            
            // Lấy vai trò
            String roleUser = authServiceClient.getRoleByUserId(user.getUserId());
            
            // Gán dữ liệu vào DTO
            userDTO.setEmail(email);
            userDTO.setId(user.getId());
            userDTO.setFullname(user.getFullname());
            userDTO.setUsername(user.getUsername());
            userDTO.setPhone(user.getPhone());
            userDTO.setAddress(user.getAddress());
            userDTO.setGender(user.isGender());
            userDTO.setDob(user.getDob());
            userDTO.setRole(roleUser);
            
            return userDTO;
        });
    }
      // Fallback method for findAllByIdsPaginated
    public Page<UserDTO> findAllByIdsPaginatedFallback(List<Long> ids, int page, int size, Throwable t) {
        logger.error("Circuit breaker triggered for findAllByIdsPaginated. Error: {}", t.getMessage(), t);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            // Tạo Specification để lọc theo danh sách ID
            Specification<User> spec = hasUserIdIn(ids);
            Page<User> userPage = userRepository.findAll(spec, pageable);
            
            // Chuyển đổi sang DTO nhưng bỏ qua thông tin từ auth-service
            return userPage.map(user -> {
                UserDTO userDTO = new UserDTO();
                userDTO.setEmail(""); // Không có thông tin email
                userDTO.setId(user.getId());
                userDTO.setFullname(user.getFullname());
                userDTO.setUsername(user.getUsername());
                userDTO.setPhone(user.getPhone());
                userDTO.setAddress(user.getAddress());
                userDTO.setGender(user.isGender());
                userDTO.setDob(user.getDob());
                userDTO.setRole(""); // Không có thông tin vai trò
                
                return userDTO;
            });
        } catch (Exception ex) {
            logger.error("Error in fallback method: {}", ex.getMessage(), ex);
            // Trả về Page rỗng
            return new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), 0);
        }
    }

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "filterUsersPaginatedFallback")
    public Page<UserDTO> filterUsersPaginated(String keyword, String gender, String role, int page, int size) {
        Specification<User> spec = Specification.where(null);
        
        // Thêm điều kiện tìm kiếm theo từ khóa
        if (keyword != null && !keyword.isEmpty()) {
            logger.debug("Filtering by keyword: {}", keyword);
            spec = spec.and(containsKeyword(keyword));
        }
        
        // Thêm điều kiện lọc theo giới tính
        if (gender != null && !gender.isEmpty()) {
            logger.debug("Filtering by gender: {}", gender);
            spec = spec.and(hasGender(gender.equals("Male")));
        }
        
        // Thêm điều kiện lọc theo vai trò
        if (role != null && !role.isEmpty()) {
            logger.debug("Filtering by role: {}", role);
            List<Long> userIds = authServiceClient.getUserIdsByRole(role);
            if (userIds.size() > 0) {
                spec = spec.and(hasUserIdIn(userIds));
            }
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(spec, pageable);
        
        return userPage.map(user -> {
            UserDTO userDTO = new UserDTO();
            
            try {
                // Lấy email từ auth service
                ResponseEntity<Map<String, Object>> responseEmail = authServiceClient.getAuthUserEmailById(user.getUserId());
                String email = responseEmail.getBody().get("data").toString();
                userDTO.setEmail(email);
                
                // Lấy role từ auth service
                String roleUser = authServiceClient.getRoleByUserId(user.getUserId());
                userDTO.setRole(roleUser);
            } catch (Exception e) {
                logger.error("Error fetching data from auth service for user ID {}: {}", user.getUserId(), e.getMessage());
            }
            
            // Thiết lập các trường dữ liệu khác
            userDTO.setId(user.getId());
            userDTO.setFullname(user.getFullname());
            userDTO.setUsername(user.getUsername());
            userDTO.setPhone(user.getPhone());
            userDTO.setAddress(user.getAddress());
            userDTO.setGender(user.isGender());
            userDTO.setDob(user.getDob());
            
            return userDTO;
        });
    }
    
    // Phương thức fallback cho filterUsersPaginated
    public Page<UserDTO> filterUsersPaginatedFallback(String keyword, String gender, String role, int page, int size, Throwable t) {
        logger.error("Circuit breaker triggered for filterUsersPaginated. Error: {}", t.getMessage(), t);
        
        try {
            Specification<User> spec = Specification.where(null);

            if (keyword != null && !keyword.isEmpty()) {
                spec = spec.and(containsKeyword(keyword));
            }

            if (gender != null && !gender.isEmpty()) {
                spec = spec.and(hasGender(gender.equals("Male")));
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<User> userPage = userRepository.findAll(spec, pageable);
            
            return userPage.map(user -> {
                UserDTO userDTO = new UserDTO();
                userDTO.setId(user.getId());
                userDTO.setFullname(user.getFullname());
                userDTO.setUsername(user.getUsername());
                userDTO.setPhone(user.getPhone());
                userDTO.setAddress(user.getAddress());
                userDTO.setGender(user.isGender());
                userDTO.setDob(user.getDob());
                // Trong trường hợp fallback, không có thông tin email và role
                return userDTO;
            });
        } catch (Exception ex) {
            logger.error("Error in fallback method: {}", ex.getMessage(), ex);
            return new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), 0);
        }
    }

    public static Specification<User> hasGender(Boolean gender) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("gender"), gender);
    }

    public static Specification<User> hasUserIdIn(List<Long> userIds) {
        return (root, query, criteriaBuilder) -> root.get("id").in(userIds);
    }

    public static Specification<User> containsKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            String likePattern = "%" + keyword.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), likePattern), // Tìm theo tên
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), likePattern),    // Tìm theo số điện thoại
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), likePattern)  // Tìm theo address
            );
        };
    }
}
