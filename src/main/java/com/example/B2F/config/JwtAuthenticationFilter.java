package com.example.B2F.config;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private JwtService jwtService;
    private UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService){
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            if(request.getServletPath().contains("/api/B2F/register") || request.getServletPath().contains("/api/B2F/login")){
                filterChain.doFilter(request, response);
                return;
            }
            final String header = request.getHeader("Authorization");
            final String jwt;
            final String userEmail;

            if (header == null || !header.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            jwt = header.substring(7);
            userEmail = jwtService.extractUsername(jwt);
            System.out.println("JWT" + jwt);
            System.out.println("userEmail" + userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                var isTokenValid = jwtService.isTokenValid(jwt, userDetails);
                if (isTokenValid) {
                    UsernamePasswordAuthenticationToken passwordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    passwordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(passwordAuthenticationToken);
                }
            }

        } catch (ExpiredJwtException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Token Expired. Please log in again.");
            response.getWriter().flush();
        } catch (Exception e){
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token or authentication failed.");
        }finally {
            filterChain.doFilter(request, response);
        }
    }
}
