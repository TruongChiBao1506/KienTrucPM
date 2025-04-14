package iuh.fit.se.userservice.services.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
import co.elastic.clients.elasticsearch.core.DeleteByQueryResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.se.userservice.dtos.AuthUser;
import iuh.fit.se.userservice.entities.User;
import iuh.fit.se.userservice.feign.AuthServiceClient;
import iuh.fit.se.userservice.index.UserIndex;
import iuh.fit.se.userservice.repositories.UserElasticsearchRepository;
import iuh.fit.se.userservice.repositories.UserRepository;
import iuh.fit.se.userservice.services.UserIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserIndexServiceImpl implements UserIndexService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthServiceClient authServiceClient;

    @Autowired
    private UserElasticsearchRepository userElasticsearchRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private final ElasticsearchOperations elasticsearchOperations;
    @Autowired
    private ElasticsearchClient elasticsearchClient;

    public UserIndexServiceImpl(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public void syncUsersToElasticsearch() {
        userElasticsearchRepository.deleteAll();
        List<User> users = userRepository.findAll();

        List<UserIndex> userIndexes = users.stream().map(user -> {
            ResponseEntity<Map<String, Object>> authResponse = authServiceClient.getAuthUser(user.getUserId());
            if(authResponse.getStatusCode() != HttpStatus.OK){
                throw new RuntimeException("Không tìm thấy thông tin người dùng");
            }
            else{
                Map<String, Object> responseBody = authResponse.getBody();
                AuthUser authUser = objectMapper.convertValue(responseBody.get("data"), AuthUser.class);
                UserIndex userIndex = new UserIndex();
                userIndex.setUserId(user.getUserId());
                userIndex.setUsername(authUser.getUsername());
                userIndex.setFullname(user.getFullname());
                userIndex.setEmail(authUser.getEmail());
                userIndex.setAddress(user.getAddress());
                userIndex.setPhone(user.getPhone());
                userIndex.setGender(user.isGender());
                userIndex.setRole(authUser.getRole());
                return userIndex;
            }

        }).collect(Collectors.toList());
        userElasticsearchRepository.saveAll(userIndexes);
    }

    @Override
    public void addUserToElasticsearch(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        ResponseEntity<Map<String, Object>> authResponse = authServiceClient.getAuthUser(user.getUserId());
        if (authResponse.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Không tìm thấy thông tin auth của người dùng");
        }

        Map<String, Object> responseBody = authResponse.getBody();
        AuthUser authUser = objectMapper.convertValue(responseBody.get("data"), AuthUser.class);

        UserIndex userIndex = userElasticsearchRepository.findByUserId(userId).orElse(new UserIndex());

        userIndex.setUserId(userId);
        userIndex.setUsername(authUser.getUsername());
        userIndex.setFullname(user.getFullname());
        userIndex.setEmail(authUser.getEmail());
        userIndex.setAddress(user.getAddress());
        userIndex.setPhone(user.getPhone());
        userIndex.setGender(user.isGender());
        userIndex.setRole(authUser.getRole());

        userElasticsearchRepository.save(userIndex);
    }
    @Override
    public void deleteUserByUserId(Long userId){
        try {
            // 🔥 Tạo truy vấn BoolQuery
            BoolQuery.Builder boolQuery = new BoolQuery.Builder();
            boolQuery.must(m -> m.term(t -> t.field("userId").value(userId)));

            Query query = Query.of(q -> q.bool(boolQuery.build()));

            // 🔥 Xóa bằng DeleteByQueryRequest
            DeleteByQueryRequest request = DeleteByQueryRequest.of(d -> d
                    .index("users")
                    .query(query)
            );

            DeleteByQueryResponse response = elasticsearchClient.deleteByQuery(request);

            // 📌 Kiểm tra xem có bản ghi nào bị xóa không
            long deletedCount = response.deleted();
            if (deletedCount == 0) {
                System.out.println("Không tìm thấy document với userId: " + userId);
            } else {
                System.out.println("Đã xóa " + deletedCount + " document(s) với userId: " + userId);
            }

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi xóa userId: " + userId, e);
        }
    }
}
