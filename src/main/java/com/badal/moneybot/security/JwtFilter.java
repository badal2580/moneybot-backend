package com.badal.moneybot.security;

import com.badal.moneybot.entity.User;
import com.badal.moneybot.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public JwtFilter(
            JwtService jwtService,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader =
                request.getHeader("Authorization");

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                authHeader.substring(7).trim();

        if (token.isBlank()) {
            sendUnauthorizedResponse(
                    response,
                    "JWT token is missing"
            );
            return;
        }

        try {
            if (!jwtService.isTokenValid(token)) {
                sendUnauthorizedResponse(
                        response,
                        "JWT token is invalid or expired"
                );
                return;
            }

            String email =
                    jwtService.extractEmail(token);

            if (email == null || email.isBlank()) {
                sendUnauthorizedResponse(
                        response,
                        "JWT token does not contain a valid user"
                );
                return;
            }

            if (SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                User user =
                        userRepository.findByEmail(email)
                                .orElse(null);

                if (user == null) {
                    sendUnauthorizedResponse(
                            response,
                            "User associated with token was not found"
                    );
                    return;
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user.getEmail(),
                                null,
                                Collections.emptyList()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (Exception exception) {

            SecurityContextHolder.clearContext();

            sendUnauthorizedResponse(
                    response,
                    "JWT token is invalid or expired"
            );
        }
    }

    private void sendUnauthorizedResponse(
            HttpServletResponse response,
            String message
    ) throws IOException {

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType(
                "application/json"
        );

        Map<String, Object> responseBody =
                new LinkedHashMap<>();

        responseBody.put("success", false);
        responseBody.put("message", message);
        responseBody.put("data", null);

        objectMapper.writeValue(
                response.getWriter(),
                responseBody
        );
    }
}
