package iuh.fit.se.authservice.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.se.authservice.dtos.RegisterRequest;
import iuh.fit.se.authservice.services.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class OtpServiceImpl implements OtpService {

    private final StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public OtpServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    @Override
    public void saveRequestAndOtp(RegisterRequest registerRequest, String otp) {
        redisTemplate.opsForValue().set("OTP:" + registerRequest.getEmail(), otp, 5, TimeUnit.MINUTES);
        String json = null;
        try {
            json = objectMapper.writeValueAsString(registerRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        redisTemplate.opsForValue().set("UNVERIFIED_USER:" + registerRequest.getEmail(), json, 5, TimeUnit.MINUTES);
    }

    @Override
    public boolean verifyOtp(String email, String otp) {
        String savedOtp = redisTemplate.opsForValue().get("OTP:" + email);
        return otp.equals(savedOtp);
    }

    @Override
    public boolean isAllowedToSendOtp(String email) {
        String key = "RATE_LIMIT:" + email;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, 1, TimeUnit.MINUTES); // Giới hạn trong 1 phút
        }
        return count <= 3; // Tối đa 3 lần gửi trong 1 phút
    }
}
