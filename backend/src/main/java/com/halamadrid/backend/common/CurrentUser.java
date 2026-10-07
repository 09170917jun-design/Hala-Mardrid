package com.halamadrid.backend.common;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;

/** JWT에서 로그인 사용자 정보를 꺼낸다. 비로그인이면 id는 null이다. */
public record CurrentUser(Long id, boolean admin) {

    public static CurrentUser from(Jwt jwt) {
        if (jwt == null) {
            return new CurrentUser(null, false);
        }
        return new CurrentUser(Long.parseLong(jwt.getSubject()), "ADMIN".equals(jwt.getClaimAsString("role")));
    }

    public Long requireId() {
        if (id == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요합니다.");
        }
        return id;
    }
}
