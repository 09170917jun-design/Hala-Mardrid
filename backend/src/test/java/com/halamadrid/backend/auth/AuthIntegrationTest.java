package com.halamadrid.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    private String body(String email, String password, String nickname) {
        return "{\"email\":\"%s\",\"password\":\"%s\",\"nickname\":\"%s\"}".formatted(email, password, nickname);
    }

    private MvcResult signup(String email, String nickname) throws Exception {
        return mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                .content(body(email, "password123", nickname))).andReturn();
    }

    @Test
    void signupLoginAndMe() throws Exception {
        MvcResult signup = signup("fan1@example.com", "fan_one");
        assertThat(signup.getResponse().getStatus()).isEqualTo(201);
        assertThat(signup.getResponse().getHeader("Set-Cookie")).contains("refresh_token=").contains("HttpOnly");

        MvcResult login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"FAN1@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.nickname").value("fan_one"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();

        String token = JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("fan1@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void meWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void duplicateEmailAndNickname() throws Exception {
        signup("dup@example.com", "dup_nick");
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                .content(body("dup@example.com", "password123", "other_nick")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"));
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                .content(body("other@example.com", "password123", "dup_nick")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_NICKNAME"));
    }

    @Test
    void wrongPasswordIsUnauthorized() throws Exception {
        signup("wrong@example.com", "wrong_pw");
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"wrong@example.com\",\"password\":\"nope-nope\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void validationErrors() throws Exception {
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                .content(body("not-an-email", "short", "x")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void refreshRotatesTokenAndOldOneIsRejected() throws Exception {
        MvcResult signup = signup("rotate@example.com", "rotate_me");
        String oldRefresh = cookieValue(signup);

        MvcResult refreshed = mockMvc.perform(post("/api/auth/refresh")
                .cookie(new MockCookie("refresh_token", oldRefresh)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();
        String newRefresh = cookieValue(refreshed);
        assertThat(newRefresh).isNotEqualTo(oldRefresh);

        mockMvc.perform(post("/api/auth/refresh").cookie(new MockCookie("refresh_token", oldRefresh)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/refresh").cookie(new MockCookie("refresh_token", newRefresh)))
                .andExpect(status().isOk());
    }

    @Test
    void logoutInvalidatesRefreshToken() throws Exception {
        MvcResult signup = signup("bye@example.com", "bye_bye");
        String refresh = cookieValue(signup);

        mockMvc.perform(post("/api/auth/logout").cookie(new MockCookie("refresh_token", refresh)))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("refresh_token", 0));
        mockMvc.perform(post("/api/auth/refresh").cookie(new MockCookie("refresh_token", refresh)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshWithoutCookieIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")).andExpect(status().isUnauthorized());
    }

    private String cookieValue(MvcResult result) {
        String header = result.getResponse().getHeader("Set-Cookie");
        assertThat(header).isNotNull();
        return header.split(";")[0].substring("refresh_token=".length());
    }
}
