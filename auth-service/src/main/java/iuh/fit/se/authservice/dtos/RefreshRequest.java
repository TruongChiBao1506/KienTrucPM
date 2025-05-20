package iuh.fit.se.authservice.dtos;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshRequest {
    private String refreshToken;
}
