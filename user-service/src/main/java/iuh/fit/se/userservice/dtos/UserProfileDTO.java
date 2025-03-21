package iuh.fit.se.userservice.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {
    @NotEmpty(message = "tài khoản không được để trống")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Tài khoản không chứa ký tự đặc biệt")
    private String username;
    @NotEmpty(message = "Họ tên không được để trống")
    private String fullname;
    @NotEmpty(message = "Email không được để trống")
    @Pattern(regexp = "^[a-zA-Z0-9]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*", message = "Email không hợp lệ")
    private String email;
    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải nhỏ hơn ngày hiện tại")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dob;
    @NotEmpty(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại không hợp lệ")
    private String phone;
    @NotEmpty(message = "Địa chỉ không được để trống")
    @Pattern(regexp = "^[a-zA-Z0-9\\s,.'-]{3,100}$", message = "Địa chỉ không hợp lệ")
    private String address;
    private boolean gender;
}
