package iuh.fit.se.userservice.services.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import iuh.fit.se.userservice.entities.User;
import iuh.fit.se.userservice.feign.AuthServiceClient;
import iuh.fit.se.userservice.index.UserIndex;
import iuh.fit.se.userservice.repositories.UserRepository;
import iuh.fit.se.userservice.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthServiceClient authServiceClient;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

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
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> findAll() {
        return this.userRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy người dùng với ID: " + id);
        }
//        if (userRepository.findById(id).get().getOrders().size() > 0) {
//            throw new RuntimeException("Không thể xóa người dùng đã đặt hàng");
//        }
        userRepository.deleteById(id);
    }

    @Override
    public User updateUser(User user) {
        User userInDB = userRepository.findById(user.getId()).orElseThrow(() -> new RuntimeException("User not found"));

        if (userInDB == null) {
            return null;
        } else {
            userInDB.setFullname(user.getFullname());
//            userInDB.setEmail(user.getEmail());
            userInDB.setPhone(user.getPhone());
            userInDB.setAddress(user.getAddress());
            userInDB.setGender(user.isGender());
            userInDB.setDob(user.getDob());
//            userInDB.setRole(user.getRole());
            userInDB.setUsername(user.getUsername());
            userRepository.save(userInDB);
            return userInDB;
        }
    }

    @Override
    public List<User> filterUsers(String keyword, String gender, String role) {
        Specification<User> spec = Specification.where(null);
        // Thêm điều kiện tìm kiếm theo từ khóa
        if (keyword != null && !keyword.isEmpty()) {
            System.out.println(keyword);
            spec = spec.and(containsKeyword(keyword));
        }
        // Thêm điều kiện lọc theo giới tính
        if (gender != null && !gender.isEmpty()) {
            System.out.println(gender);
            spec = spec.and(hasGender(gender.equals("Male")));
        }
        if (role != null && !role.isEmpty()) {
            List<Long> userIds = authServiceClient.getUserIdsByRole(role);
            if(userIds.size() > 0) {
                spec = spec.and(hasUserIdIn(userIds));
            }
        }
        return userRepository.findAll(spec);
    }

    @Override
    public User findByUserId(Long userId) {
        return userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public List<User> findAllByIds(List<Long> ids) {
        return userRepository.findAllById(ids);
    }

    @Override
    public List<UserIndex> searchUsers(String keyword, String gender, String role) throws IOException {
        System.out.println(keyword);
        System.out.println(gender);
        System.out.println(role);
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // Tìm kiếm theo keyword (fullname, email, phone, username, address)
        if (keyword != null && !keyword.isEmpty()) {
            boolQuery.should(MatchQuery.of(m -> m.field("fullname").query(keyword))._toQuery());
            boolQuery.should(MatchQuery.of(m -> m.field("email").query(keyword))._toQuery());
            boolQuery.should(MatchQuery.of(m -> m.field("phone").query(keyword))._toQuery());
            boolQuery.should(MatchQuery.of(m -> m.field("username").query(keyword))._toQuery());
            boolQuery.should(MatchQuery.of(m -> m.field("address").query(keyword))._toQuery());
        }

        // Lọc theo giới tính
        if (gender != null) {
            boolQuery.filter(TermQuery.of(t -> t.field("gender").value(gender))._toQuery());
        }

        // Lọc theo role
        if (role != null && !role.isEmpty()) {
            boolQuery.filter(TermQuery.of(t -> t.field("role").value(role))._toQuery());
        }

        // Thực hiện truy vấn
        SearchResponse<UserIndex> response = elasticsearchClient.search(s -> s
                        .index("users")
                        .query(boolQuery.build()._toQuery()),
                UserIndex.class);

        // Lấy kết quả từ Elasticsearch
        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
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
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("fullname")), likePattern), // Tìm theo tên
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), likePattern),    // Tìm theo số điện thoại
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), likePattern)  // Tìm theo address
            );
        };
    }
}
