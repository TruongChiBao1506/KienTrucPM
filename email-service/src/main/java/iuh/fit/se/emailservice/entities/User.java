package iuh.fit.se.emailservice.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    private Long userId;
    private String username;
    private String fullname;
    private OffsetDateTime dob;
    private String phone;
    private String address;
    private boolean gender;
}
