package iuh.fit.se.authservice.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RefreshRequest {
    private String refreshToken;
}
