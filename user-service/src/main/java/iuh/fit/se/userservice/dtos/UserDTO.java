package iuh.fit.se.userservice.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    @NotEmpty(message = "Họ tên không được để trống")
    private String fullname;
    @NotEmpty(message = "Tên đăng nhập không được để trống")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Tài khoản không chứa ký tự đặc biệt")
    private String username;
    @NotEmpty(message = "Mật khẩu không được để trống")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.])[A-Za-z\\d@$!%*?&.]{8,}$",
            message = "Mật khẩu phải chứa ít nhất 1 ký tự in hoa, 1 ký tự số, 1 ký tự đặc biệt và có ít nhất 8 ký tự")
    private String password;
    @NotEmpty(message = "Email không được để trống")
    @Pattern(regexp = "^[a-zA-Z0-9]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*", message = "Email không hợp lệ")
    private String email;
    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải nhỏ hơn ngày hiện tại")
    private Date dob;
    @NotEmpty(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại không hợp lệ")
    private String phone;
    @NotEmpty(message = "Địa chỉ không được để trống")
    @Pattern(regexp = "^[a-zA-Z0-9\\s,.'-]{3,100}$", message = "Địa chỉ không hợp lệ")
    private String address;
    private boolean gender;

    private String role;
}
