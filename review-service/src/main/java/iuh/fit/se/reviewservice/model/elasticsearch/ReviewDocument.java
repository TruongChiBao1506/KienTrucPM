package iuh.fit.se.reviewservice.model.elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "reviews")
public class ReviewDocument {

    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Long)
    private Long productId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String username;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String productName;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String content;

    @Field(type = FieldType.Integer)
    private int rating;

    @Field(type = FieldType.Keyword)
    private String createdAt;

    // Helper method to generate ID from userId and productId
    public static String generateId(Long userId, Long productId) {
        return userId + "_" + productId;
    }
}
