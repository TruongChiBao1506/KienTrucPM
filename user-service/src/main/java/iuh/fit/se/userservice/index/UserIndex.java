package iuh.fit.se.userservice.index;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.UUID;

@Document(indexName = "users")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserIndex {
    @Id
    private String id;
    @Field(type = FieldType.Keyword)
    private Long userId;
    private String email;
    private String username;
    private String fullname;
    private String phone;
    private String address;
    private Boolean gender;
    private String role;

    public UserIndex() {
        this.id = UUID.randomUUID().toString(); // Tạo ID tự động nếu chưa có
    }
}
