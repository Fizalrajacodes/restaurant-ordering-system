package com.restaurant.controller.admin;

import com.restaurant.dtos.ApiResponse;
import com.restaurant.dtos.auth.LoginRequest;
import com.restaurant.dtos.auth.LoginResponse;
import com.restaurant.dtos.auth.RefreshTokenRequest;
import com.restaurant.dtos.auth.RegisterRequest;
import com.restaurant.dtos.auth.UserInfo;
import com.restaurant.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Login successful", authService.login(request));
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("Registration successful", authService.register(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success("Token refreshed successfully", authService.refreshToken(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserInfo> getCurrentUser() {
        return ApiResponse.success(authService.getCurrentUser());
    }
}
