package com.erent.users_microservice.service;

import com.erent.users_microservice.entities.AuthResponse;
import com.erent.users_microservice.entities.User;
import com.erent.users_microservice.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    @Autowired
    private JwtUtil jwt;

    public AuthResponse getAuthenticationResponse(User user) {

        String accessToken = jwt.generate(user, "ACCESS");
        String refreshToken = jwt.generate(user, "REFRESH");

        return new AuthResponse(accessToken, refreshToken);

    }
}
