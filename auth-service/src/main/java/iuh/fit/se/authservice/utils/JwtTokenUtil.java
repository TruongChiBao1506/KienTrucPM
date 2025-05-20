package iuh.fit.se.authservice.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenUtil {
    @Value("${jwt.secret}")
    private String secretKey;
    private final long jwtExpirationMs = 5 * 60 * 1000; //5 phút
    // Tạo JWT token
    public String generateResetToken(String email) {
        return Jwts.builder().setSubject(email).setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256).compact();
    }

    // Lấy thông tin email từ JWT
    public String getEmailFromToken(String token) {
        @SuppressWarnings("deprecation")
        Claims claims = Jwts.parser().setSigningKey(getSignInKey()).parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    // Xác thực token
    @SuppressWarnings("deprecation")
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(getSignInKey()).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token expired");
        } catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid token");
        }
    }
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
