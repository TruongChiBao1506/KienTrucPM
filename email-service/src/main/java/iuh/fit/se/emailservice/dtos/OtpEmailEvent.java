package iuh.fit.se.emailservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpEmailEvent {
    private String email;
    private String otp;
}
