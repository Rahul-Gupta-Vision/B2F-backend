package com.example.B2F.service;

import com.example.B2F.config.JwtService;
import com.example.B2F.entities.RefreshTokens;
import com.example.B2F.entities.TOKEN_EXP_STATE;
import com.example.B2F.entities.User;
import com.example.B2F.repository.RefreshTokenRepository;
import com.example.B2F.wrappers.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    @Value("${spring.application.security.refreshToken.expiration}")
    private int refreshTokenExpiration;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public Optional<RefreshTokens> findByToken(String token){
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshTokens createRefreshToken(User user){
        RefreshTokens refreshTokens = RefreshTokens.builder().build();
        refreshTokens.setToken(UUID.randomUUID().toString());
        refreshTokens.setCreatedAt(Instant.now());
        refreshTokens.setExpiresAt(Instant.now().plusMillis(refreshTokenExpiration));
        refreshTokens.setUser(user);
        return refreshTokens;
    }

    public TOKEN_EXP_STATE verifyExpiration(RefreshTokens token){
        if(token.getExpiresAt().compareTo(Instant.now()) < 0){
            return TOKEN_EXP_STATE.EXPIRED;
        }
        return TOKEN_EXP_STATE.UN_EXPIRED;
    }

    public RefreshTokens updateToken(User user){
        String newToken = UUID.randomUUID().toString();
        var refreshToken = refreshTokenRepository.findByUser(user)
                            .map(existingToken ->{
                                existingToken.setToken(newToken);
                                existingToken.setCreatedAt(Instant.now());
                                existingToken.setExpiresAt(Instant.now().plusMillis(refreshTokenExpiration));
                                return existingToken;
                            }).orElse(createRefreshToken(user));
        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public TokenResponse getRefreshTokenWhenExpired(String refresh_token){
        var token = findByToken(refresh_token);
        if(token.isEmpty()){
            return null;
        }
        User user = token.get().getUser();
        String jwt = jwtService.generateToken(user);
        var exp_state = verifyExpiration(token.get());
        if(exp_state == TOKEN_EXP_STATE.UN_EXPIRED){
            return TokenResponse.builder().access_token(jwt).refresh_token(refresh_token).build();
        }
        var newRefreshToken = createRefreshToken(user);
        refreshTokenRepository.save(newRefreshToken);
        return TokenResponse.builder().access_token(jwt).refresh_token(newRefreshToken.getToken()).build();
    }
}
