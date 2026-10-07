package com.halamadrid.backend.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.halamadrid.backend.auth.AuthDtos.AuthResponse;
import com.halamadrid.backend.auth.AuthDtos.AuthResult;
import com.halamadrid.backend.auth.AuthDtos.KakaoLoginRequest;
import com.halamadrid.backend.auth.AuthDtos.KakaoUrlResponse;
import com.halamadrid.backend.auth.AuthDtos.LoginRequest;
import com.halamadrid.backend.auth.AuthDtos.SignupRequest;
import com.halamadrid.backend.auth.kakao.KakaoClient;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    static final String REFRESH_COOKIE = "refresh_token";

    private final AuthService authService;
    private final KakaoClient kakaoClient;
    private final boolean cookieSecure;

    public AuthController(AuthService authService, KakaoClient kakaoClient,
            @Value("${app.cookie.secure}") boolean cookieSecure) {
        this.authService = authService;
        this.kakaoClient = kakaoClient;
        this.cookieSecure = cookieSecure;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return respond(HttpStatus.CREATED, authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return respond(HttpStatus.OK, authService.login(request));
    }

    @GetMapping("/kakao/url")
    public KakaoUrlResponse kakaoUrl(@RequestParam String redirectUri, @RequestParam String state) {
        return new KakaoUrlResponse(kakaoClient.authorizeUrl(redirectUri, state));
    }

    @PostMapping("/kakao")
    public ResponseEntity<AuthResponse> kakao(@Valid @RequestBody KakaoLoginRequest request) {
        return respond(HttpStatus.OK, authService.kakaoLogin(request.code(), request.redirectUri()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
        return respond(HttpStatus.OK, authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie("", 0).toString())
                .build();
    }

    private ResponseEntity<AuthResponse> respond(HttpStatus status, AuthResult result) {
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookie(result.refreshToken(), authService.refreshTtl().toSeconds()).toString())
                .body(result.response());
    }

    private ResponseCookie cookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(maxAgeSeconds)
                .build();
    }
}
