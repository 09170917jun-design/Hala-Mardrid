package com.halamadrid.backend.auth;

import com.halamadrid.backend.user.UserResponse;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record SignupRequest(
            @NotBlank(message = "이메일을 입력해 주세요.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            @Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
            String email,

            @NotBlank(message = "비밀번호를 입력해 주세요.")
            @Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.")
            String password,

            @NotBlank(message = "닉네임을 입력해 주세요.")
            @Size(min = 2, max = 12, message = "닉네임은 2~12자여야 합니다.")
            @Pattern(regexp = "^[0-9A-Za-z가-힣_]+$", message = "닉네임은 한글, 영문, 숫자, 밑줄(_)만 쓸 수 있습니다.")
            String nickname) {
    }

    public record LoginRequest(
            @NotBlank(message = "이메일을 입력해 주세요.") String email,
            @NotBlank(message = "비밀번호를 입력해 주세요.") String password) {
    }

    public record AuthResponse(String accessToken, long expiresIn, UserResponse user) {
    }

    /** 서비스 내부용: 컨트롤러가 refreshToken은 쿠키로, 나머지는 본문으로 내려보낸다. */
    public record AuthResult(AuthResponse response, String refreshToken) {
    }
}
