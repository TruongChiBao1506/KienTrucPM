package iuh.fit.se.chatbotservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationSuggestion {
    private String url;             // URL đầy đủ để điều hướng
    private String title;           // Tiêu đề hiển thị của button
    private String description;     // Mô tả ngắn về đích đến
    private String type;            // Loại điều hướng: "category", "product", "search", etc.
    private boolean autoRedirect;   // True nếu muốn tự động chuyển hướng (thường để false)
}