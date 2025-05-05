package iuh.fit.se.authservice.utils;

import java.security.SecureRandom;

public class OtpGenerator {
    private static final SecureRandom random = new SecureRandom();
    private static final int OTP_LENGTH = 6;

    public static String generateOtp() {
        int otp = 100000 + random.nextInt(900000); // Tạo OTP 6 chữ số
        return String.valueOf(otp);
    }
}
