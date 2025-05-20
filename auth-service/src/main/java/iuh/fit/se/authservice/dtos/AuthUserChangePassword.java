package iuh.fit.se.authservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthUserChangePassword {
    private String username;
    private String password;
    private String newPassword;
}
