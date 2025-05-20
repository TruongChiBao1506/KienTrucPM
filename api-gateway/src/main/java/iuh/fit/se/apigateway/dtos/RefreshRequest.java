package iuh.fit.se.apigateway.dtos;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRequest {
    private String refreshToken;
}
