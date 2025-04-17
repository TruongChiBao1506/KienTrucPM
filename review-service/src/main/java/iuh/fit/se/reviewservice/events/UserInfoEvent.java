package iuh.fit.se.reviewservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoEvent {
    private Long id;
    private String username;
    private String email;
}