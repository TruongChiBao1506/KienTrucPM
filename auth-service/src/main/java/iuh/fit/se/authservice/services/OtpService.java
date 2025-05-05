package iuh.fit.se.authservice.services;

import iuh.fit.se.authservice.dtos.RegisterRequest;
import org.springframework.data.redis.core.StringRedisTemplate;

public interface OtpService {
    public void saveRequestAndOtp(RegisterRequest registerRequest, String otp);
    public boolean verifyOtp(String email, String otp);
    public boolean isAllowedToSendOtp(String email);

}
