package iuh.fit.se.userservice.events.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEmailUpdateResponseEvent {
    private Long userId;
    private boolean success;
}
