package com.halamadrid.backend.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.halamadrid.backend.auth.AuthDtos.AuthResponse;
import com.halamadrid.backend.auth.AuthDtos.AuthResult;
import com.halamadrid.backend.auth.AuthDtos.LoginRequest;
import com.halamadrid.backend.auth.AuthDtos.SignupRequest;
import com.halamadrid.backend.auth.kakao.KakaoClient;
import com.halamadrid.backend.auth.kakao.KakaoProfile;
import com.halamadrid.backend.common.ApiException;
import com.halamadrid.backend.user.User;
import com.halamadrid.backend.user.UserRepository;
import com.halamadrid.backend.user.UserResponse;

@Service
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final KakaoClient kakaoClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public AuthService(UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            SocialAccountRepository socialAccountRepository,
            KakaoClient kakaoClient,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.access-minutes}") long accessMinutes,
            @Value("${app.jwt.refresh-days}") long refreshDays) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.socialAccountRepository = socialAccountRepository;
        this.kakaoClient = kakaoClient;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.accessTtl = Duration.ofMinutes(accessMinutes);
        this.refreshTtl = Duration.ofDays(refreshDays);
    }

    public Duration refreshTtl() {
        return refreshTtl;
    }

    @Transactional
    public AuthResult signup(SignupRequest request) {
        String email = request.email().trim().toLowerCase();
        String nickname = request.nickname().trim();
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "이미 가입된 이메일입니다.");
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new ApiException(HttpStatus.CONFLICT, "DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다.");
        }
        User user = userRepository.save(User.createWithEmail(email, passwordEncoder.encode(request.password()), nickname));
        return issue(user);
    }

    @Transactional
    public AuthResult login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .filter(u -> u.getPasswordHash() != null && passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                        "이메일 또는 비밀번호가 올바르지 않습니다."));
        return issue(user);
    }

    /** 카카오 계정으로 로그인한다. 처음이면 회원을 만든다. */
    @Transactional
    public AuthResult kakaoLogin(String code, String redirectUri) {
        KakaoProfile profile = kakaoClient.fetchProfile(code, redirectUri);
        User user = socialAccountRepository
                .findByProviderAndProviderId(SocialAccount.Provider.KAKAO, profile.id())
                .map(SocialAccount::getUser)
                .orElseGet(() -> {
                    User created = userRepository.save(User.createSocial(uniqueNickname(profile.nickname())));
                    socialAccountRepository.save(new SocialAccount(created, SocialAccount.Provider.KAKAO, profile.id()));
                    return created;
                });
        return issue(user);
    }

    private String uniqueNickname(String raw) {
        String base = raw == null ? "" : raw.replaceAll("[^0-9A-Za-z가-힣_]", "");
        if (base.length() < 2) {
            base = "마드리디스타";
        }
        if (base.length() > 12) {
            base = base.substring(0, 12);
        }
        String candidate = base;
        for (int i = 0; userRepository.existsByNickname(candidate) && i < 20; i++) {
            String suffix = String.valueOf(1000 + RANDOM.nextInt(9000));
            candidate = base.substring(0, Math.min(base.length(), 8)) + suffix;
        }
        return candidate;
    }

    /** 리프레시 토큰은 1회용이다. 사용하면 폐기하고 새 토큰을 발급한다. */
    @Transactional
    public AuthResult refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "NO_REFRESH_TOKEN", "로그인이 필요합니다.");
        }
        String hash = hash(rawRefreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "다시 로그인해 주세요."));
        refreshTokenRepository.deleteByTokenHash(hash);
        if (stored.isExpired()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "EXPIRED_REFRESH_TOKEN", "로그인이 만료되었습니다. 다시 로그인해 주세요.");
        }
        return issue(stored.getUser());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenRepository.deleteByTokenHash(hash(rawRefreshToken));
        }
    }

    private AuthResult issue(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiresAt(now.plus(accessTtl))
                .claim("role", user.getRole().name())
                .build();
        String accessToken = jwtEncoder
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String rawRefresh = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        refreshTokenRepository.save(new RefreshToken(user, hash(rawRefresh), now.plus(refreshTtl)));

        AuthResponse response = new AuthResponse(accessToken, accessTtl.toSeconds(), UserResponse.from(user));
        return new AuthResult(response, rawRefresh);
    }

    private static String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
