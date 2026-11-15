package com.supportdesk.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JwtAuthenticationFilter: Intercepts every incoming request to validate the Bearer JWT token.
 * 
 * [English]:
 * - Reads 'Authorization' header.
 * - Extracts Bearer token and validates signature and expiration using JwtUtil.
 * - If valid and user is active, sets the authentication into Spring SecurityContextHolder.
 * - Never logs passwords, tokens, or private secrets.
 * 
 * [Hinglish]:
 * - Har request ke Authorization header se Bearer token check karta hai.
 * - Token valid hone par user ko authenticate karta hai taaki security rules apply ho sakein.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (!token.isEmpty()) {
                try {
                    String email = jwtUtil.getEmailFromToken(token);
                    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                        if (userDetails.isEnabled() && jwtUtil.isTokenValidForPassword(token, userDetails.getPassword())) {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails,
                                            null,
                                            userDetails.getAuthorities()
                                    );
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        } else {
                            // Account is deactivated or token was invalidated due to a password change
                            SecurityContextHolder.clearContext();
                        }
                    }
                } catch (JWTVerificationException ex) {
                    // Token expired or invalid: clear context so AuthenticationEntryPoint handles as 401
                    SecurityContextHolder.clearContext();
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
