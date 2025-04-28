package com.example.B2F.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.lang.Function;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class JwtService {
    @Value("${spring.application.security.jwt.secret-key}")
    private  String secretKey;

    @Value("${spring.application.security.jwt.expiration}")
    private  long expiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Objects> claims, UserDetails userDetails){
        return buildToken(claims, userDetails, expiration);
    }

    private String buildToken(Map<String, Objects> claims, UserDetails userDetails, Long expiration){
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+expiration))
                .signWith(getSigningkey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Date extractExpirationDateFromToken(String token)
    {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired (String token)
    {
        return extractExpirationDateFromToken(token).before(new Date());
    }
    public boolean isTokenValid(String token, UserDetails userDetails)
    {
        String userName = extractUsername(token);
        return (userName.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }


    public Claims extractAllClaims(String token){
        return Jwts.parser()
                .setSigningKey(getSigningkey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSigningkey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }


}
