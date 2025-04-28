package com.example.B2F.controllers;

import com.example.B2F.entities.User;
import com.example.B2F.service.AuthenticationService;
import com.example.B2F.service.RefreshTokenService;
import com.example.B2F.wrappers.LoginUser;
import com.example.B2F.wrappers.RefreshTokenRequest;
import com.example.B2F.wrappers.RegisterUser;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/B2F")
@RequiredArgsConstructor
public class UserController {
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    @GetMapping("/demo")
    String getDemo(){
        System.out.println(SecurityContextHolder.getContext().getAuthentication());
        return "Hello world";
    }

    @PostMapping("/register")
    ResponseEntity<?>registerUser(@RequestBody RegisterUser registerUser){
        var reg = authenticationService.register(registerUser);
        if(null == reg){
            return new ResponseEntity<>("User Already Exists", HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(reg, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    ResponseEntity<?>loginUser(@RequestBody LoginUser loginUser) throws Exception{
        return ResponseEntity.ok(authenticationService.login(loginUser));
    }

    @PostMapping("/refresh_token")
    ResponseEntity<?>refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest){
        String refresh_token = refreshTokenRequest.getRefresh_token();
        var token = refreshTokenService.getRefreshTokenWhenExpired(refresh_token);
        if(null == token){
            return new ResponseEntity<>("Invalid Refresh Token", HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(token, HttpStatus.OK);
    }
}
