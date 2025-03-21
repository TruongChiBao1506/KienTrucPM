package iuh.fit.se.authservice.services;

import iuh.fit.se.authservice.entities.RefreshToken;
import iuh.fit.se.authservice.entities.User;

import java.util.Optional;

public interface RefreshTokenService {
    public RefreshToken createRefreshToken(String username);
    public Optional<RefreshToken> findByToken(String token);
    public boolean verifyExpiration(RefreshToken token);
    public void deleteByUser(User user);
    public Optional<RefreshToken> findByUser(User user);
    public boolean isExpired(RefreshToken token);
}
