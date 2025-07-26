package br.edu.ifpb.ifmeetup.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifpb.ifmeetup.controller.contract.AuthApiContract;
import br.edu.ifpb.ifmeetup.dto.auth.request.ForgotPasswordRequest;
import br.edu.ifpb.ifmeetup.dto.auth.request.LoginRequest;
import br.edu.ifpb.ifmeetup.dto.auth.request.PasswordResetRequest;
import br.edu.ifpb.ifmeetup.dto.auth.request.RegisterRequest;
import br.edu.ifpb.ifmeetup.dto.auth.response.AuthResponse;

import br.edu.ifpb.ifmeetup.exception.AuthenticationException;
import br.edu.ifpb.ifmeetup.exception.EmailNotVerifiedException;
import br.edu.ifpb.ifmeetup.exception.ExternalServiceException;
import br.edu.ifpb.ifmeetup.integration.suap.dto.SuapLoginRequest;
import br.edu.ifpb.ifmeetup.integration.suap.service.SuapAuthService;
import br.edu.ifpb.ifmeetup.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApiContract {

    private final AuthService authService;
    private final SuapAuthService suapAuthService;
    
    @Value("${application.url:http://localhost:8080}")
    private String applicationUrl;

    @Override
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        try {
            return ResponseEntity.ok(authService.login(request, response));
        } catch (EmailNotVerifiedException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(AuthResponse.error(e.getMessage()));
        }
    }

    @Override
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.logout(request, response));
    }

    @Override
    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @Override
    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @Override
    @GetMapping("/verify")
    public ResponseEntity<AuthResponse> verifyAccount(@RequestParam("token") String token) {
        AuthResponse response = authService.verifyAccount(token);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me() {
        return ResponseEntity.ok(authService.me());
    }

    @Override
    @PostMapping("/suap/login")
    public ResponseEntity<AuthResponse> loginWithSuap(
            @Valid @RequestBody SuapLoginRequest request,
            HttpServletResponse response) {
        try {
            return ResponseEntity.ok(suapAuthService.loginWithSuap(request, response));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.error(e.getMessage()));
        } catch (ExternalServiceException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(AuthResponse.error(e.getMessage()));
        }
    }
} 