package com.example.B2F.service;

import com.example.B2F.config.JwtService;
import com.example.B2F.entities.User;
import com.example.B2F.repository.UserRepository;
import com.example.B2F.wrappers.LoginUser;
import com.example.B2F.wrappers.RegisterUser;
import com.example.B2F.wrappers.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public TokenResponse register(RegisterUser registerUser){
        var userExist = userRepository.findUserByUsername(registerUser.getUsername());
        if(userExist.isPresent()){
            return null;
        }
        var user = User.builder()
                .name(registerUser.getName())
                .username(registerUser.getUsername())
                .password(passwordEncoder.encode(registerUser.getPassword())).build();
        userRepository.save(user);
        String jwt = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.updateToken(user);
        return TokenResponse.builder().access_token(jwt).refresh_token(refreshToken.getToken()).build();
    }

    public TokenResponse login(LoginUser loginUser) throws Exception{
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword()));
            var user = userRepository.findUserByUsername(loginUser.getUsername()).orElseThrow(()->new UsernameNotFoundException("User not found"));
            var jwtToken = jwtService.generateToken(user);
            var refreshToken = refreshTokenService.updateToken(user);
            return TokenResponse.builder().access_token(jwtToken).refresh_token(refreshToken.getToken()).build();
        }catch (Exception e){
            throw new BadCredentialsException("Invalid Credentials");
        }
    }

}
