package iuh.fit.se.authservice.dtos;

import iuh.fit.se.authservice.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthUser {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String role;
}
