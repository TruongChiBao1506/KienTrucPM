package iuh.fit.se.emailservice.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {
    
    @NotBlank(message = "Người nhận không được để trống")
    @Email(message = "Email không hợp lệ")
    private String to;
    
    @NotBlank(message = "Chủ đề không được để trống")
    private String subject;
    
    @NotBlank(message = "Nội dung không được để trống")
    private String body;
    
    private String cc;
    
    private String bcc;
    
    private boolean html = false;
}
