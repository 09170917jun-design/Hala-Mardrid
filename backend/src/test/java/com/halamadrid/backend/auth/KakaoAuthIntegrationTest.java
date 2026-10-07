package com.halamadrid.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.halamadrid.backend.auth.kakao.KakaoClient;
import com.halamadrid.backend.auth.kakao.KakaoProfile;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class KakaoAuthIntegrationTest {

    private static final String REDIRECT = "http://localhost:5173/auth/kakao/callback";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    KakaoClient kakaoClient;

    private MvcResult kakaoLogin(String code) throws Exception {
        return mockMvc.perform(post("/api/auth/kakao").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"%s\",\"redirectUri\":\"%s\"}".formatted(code, REDIRECT))).andReturn();
    }

    @Test
    void firstLoginCreatesUserAndSecondLoginReusesIt() throws Exception {
        when(kakaoClient.fetchProfile("code-1", REDIRECT)).thenReturn(new KakaoProfile("900001", "카카오 팬!"));
        when(kakaoClient.fetchProfile("code-2", REDIRECT)).thenReturn(new KakaoProfile("900001", "카카오 팬!"));

        MvcResult first = kakaoLogin("code-1");
        assertThat(first.getResponse().getStatus()).isEqualTo(200);
        assertThat(first.getResponse().getHeader("Set-Cookie")).contains("refresh_token=").contains("HttpOnly");
        Integer firstId = JsonPath.read(first.getResponse().getContentAsString(), "$.user.id");
        // 특수문자는 제거된다.
        String nickname = JsonPath.read(first.getResponse().getContentAsString(), "$.user.nickname");
        assertThat(nickname).isEqualTo("카카오팬");
        assertThat((Object) JsonPath.read(first.getResponse().getContentAsString(), "$.user.email")).isNull();

        MvcResult second = kakaoLogin("code-2");
        Integer secondId = JsonPath.read(second.getResponse().getContentAsString(), "$.user.id");
        assertThat(secondId).isEqualTo(firstId);

        String token = JsonPath.read(second.getResponse().getContentAsString(), "$.accessToken");
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("카카오팬"));
    }

    @Test
    void duplicateNicknameGetsSuffix() throws Exception {
        when(kakaoClient.fetchProfile("a", REDIRECT)).thenReturn(new KakaoProfile("900101", "같은닉네임"));
        when(kakaoClient.fetchProfile("b", REDIRECT)).thenReturn(new KakaoProfile("900102", "같은닉네임"));

        String n1 = JsonPath.read(kakaoLogin("a").getResponse().getContentAsString(), "$.user.nickname");
        String n2 = JsonPath.read(kakaoLogin("b").getResponse().getContentAsString(), "$.user.nickname");
        assertThat(n1).isEqualTo("같은닉네임");
        assertThat(n2).isNotEqualTo(n1).startsWith("같은닉네임");
    }

    @Test
    void missingNicknameFallsBackToDefault() throws Exception {
        when(kakaoClient.fetchProfile("n", REDIRECT)).thenReturn(new KakaoProfile("900201", null));
        String nickname = JsonPath.read(kakaoLogin("n").getResponse().getContentAsString(), "$.user.nickname");
        assertThat(nickname).startsWith("마드리디스타");
    }

    @Test
    void authorizeUrlEndpointReturnsUrl() throws Exception {
        when(kakaoClient.authorizeUrl(REDIRECT, "s1")).thenReturn("https://kauth.kakao.com/oauth/authorize?x=1");
        mockMvc.perform(get("/api/auth/kakao/url").param("redirectUri", REDIRECT).param("state", "s1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://kauth.kakao.com/oauth/authorize?x=1"));
    }

    @Test
    void requestWithoutCodeIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/kakao").contentType(MediaType.APPLICATION_JSON)
                .content("{\"redirectUri\":\"" + REDIRECT + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
