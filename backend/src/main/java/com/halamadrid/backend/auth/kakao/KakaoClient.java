package com.halamadrid.backend.auth.kakao;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import com.halamadrid.backend.common.ApiException;

/** 카카오 OAuth(인가 코드 방식) 호출을 담당한다. */
@Component
public class KakaoClient {

    private static final Logger log = LoggerFactory.getLogger(KakaoClient.class);

    private static final String AUTHORIZE_URL = "https://kauth.kakao.com/oauth/authorize";
    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String ME_URL = "https://kapi.kakao.com/v2/user/me";

    private static final ParameterizedTypeReference<Map<String, Object>> MAP = new ParameterizedTypeReference<>() {
    };

    private final RestClient http = RestClient.create();
    private final String clientId;
    private final String clientSecret;
    private final List<String> allowedRedirectUris;

    public KakaoClient(@Value("${app.kakao.client-id:}") String clientId,
            @Value("${app.kakao.client-secret:}") String clientSecret,
            @Value("${app.kakao.allowed-redirect-uris}") String allowedRedirectUris) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.allowedRedirectUris = Arrays.stream(allowedRedirectUris.split(",")).map(String::trim)
                .filter(s -> !s.isEmpty()).toList();
    }

    public String authorizeUrl(String redirectUri, String state) {
        validate(redirectUri);
        return UriComponentsBuilder.fromUriString(AUTHORIZE_URL)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("state", state)
                .build().encode().toUriString();
    }

    public KakaoProfile fetchProfile(String code, String redirectUri) {
        validate(redirectUri);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);
        form.add("code", code);

        try {
            Map<String, Object> token = http.post().uri(TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form).retrieve().body(MAP);
            String accessToken = token == null ? null : (String) token.get("access_token");
            if (accessToken == null) {
                throw failed("카카오 토큰을 받지 못했습니다.");
            }

            Map<String, Object> me = http.get().uri(ME_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve().body(MAP);
            if (me == null || !(me.get("id") instanceof Number id)) {
                throw failed("카카오 사용자 정보를 받지 못했습니다.");
            }
            return new KakaoProfile(String.valueOf(id.longValue()), extractNickname(me));
        } catch (RestClientException e) {
            // 코드/시크릿/리다이렉트 URI 불일치 등. 상세 원인은 서버 로그에만 남긴다.
            log.warn("Kakao API call failed: {}", e.getMessage());
            throw failed("카카오 로그인에 실패했습니다. 다시 시도해 주세요.");
        }
    }

    private void validate(String redirectUri) {
        if (clientId.isBlank() || clientSecret.isBlank()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "KAKAO_NOT_CONFIGURED", "카카오 로그인이 아직 설정되지 않았습니다.");
        }
        if (!allowedRedirectUris.contains(redirectUri)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REDIRECT_URI", "허용되지 않은 리다이렉트 주소입니다.");
        }
    }

    @SuppressWarnings("unchecked")
    private String extractNickname(Map<String, Object> me) {
        if (me.get("kakao_account") instanceof Map<?, ?> account
                && ((Map<String, Object>) account).get("profile") instanceof Map<?, ?> profile
                && ((Map<String, Object>) profile).get("nickname") instanceof String nickname) {
            return nickname;
        }
        if (me.get("properties") instanceof Map<?, ?> props && ((Map<String, Object>) props).get("nickname") instanceof String nickname) {
            return nickname;
        }
        return null;
    }

    private ApiException failed(String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, "KAKAO_AUTH_FAILED", message);
    }
}
