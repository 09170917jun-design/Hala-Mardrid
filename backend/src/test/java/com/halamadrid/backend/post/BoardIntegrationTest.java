package com.halamadrid.backend.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class BoardIntegrationTest {

    private static final AtomicInteger SEQ = new AtomicInteger();

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbc;

    /** 새 회원을 만들고 액세스 토큰을 돌려준다. */
    private String newUser() throws Exception {
        int n = SEQ.incrementAndGet();
        MvcResult r = mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"board%d@example.com\",\"password\":\"password123\",\"nickname\":\"bu%d\"}".formatted(n, n)))
                .andExpect(status().isCreated()).andReturn();
        return JsonPath.read(r.getResponse().getContentAsString(), "$.accessToken");
    }

    private String newAdmin() throws Exception {
        int n = SEQ.incrementAndGet();
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin%d@example.com\",\"password\":\"password123\",\"nickname\":\"ad%d\"}".formatted(n, n)))
                .andExpect(status().isCreated());
        jdbc.update("update users set role = 'ADMIN' where email = ?", "admin%d@example.com".formatted(n));
        // 역할은 토큰에 들어 있으므로 다시 로그인해서 새 토큰을 받는다.
        MvcResult r = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin%d@example.com\",\"password\":\"password123\"}".formatted(n)))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(r.getResponse().getContentAsString(), "$.accessToken");
    }

    private int createPost(String token, String title) throws Exception {
        MvcResult r = mockMvc.perform(post("/api/posts").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"%s\",\"content\":\"내용입니다\"}".formatted(title)))
                .andExpect(status().isCreated()).andReturn();
        return JsonPath.read(r.getResponse().getContentAsString(), "$.id");
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    void createListAndReadWithoutLogin() throws Exception {
        String token = newUser();
        int id = createPost(token, "첫 글");

        mockMvc.perform(get("/api/posts")).andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].title").value("첫 글"))
                .andExpect(jsonPath("$.items[0].authorNickname").isNotEmpty());

        // 로그인 없이 상세 조회 가능, 조회수 증가
        mockMvc.perform(get("/api/posts/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("내용입니다"))
                .andExpect(jsonPath("$.viewCount").value(1))
                .andExpect(jsonPath("$.likedByMe").value(false));
        mockMvc.perform(get("/api/posts/" + id)).andExpect(jsonPath("$.viewCount").value(2));
    }

    @Test
    void writingRequiresLogin() throws Exception {
        mockMvc.perform(post("/api/posts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"t\",\"content\":\"c\"}")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/posts/1/like")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/posts/1/comments").contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"c\"}")).andExpect(status().isUnauthorized());
    }

    @Test
    void validation() throws Exception {
        String token = newUser();
        mockMvc.perform(post("/api/posts").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"  \",\"content\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/posts/999999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"));
    }

    @Test
    void onlyAuthorCanEdit() throws Exception {
        String author = newUser();
        String other = newUser();
        int id = createPost(author, "수정 테스트");
        String body = "{\"title\":\"수정됨\",\"content\":\"새 내용\"}";

        mockMvc.perform(put("/api/posts/" + id).header("Authorization", bearer(other))
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        mockMvc.perform(put("/api/posts/" + id).header("Authorization", bearer(author))
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("수정됨"));
    }

    @Test
    void likeTogglesAndCounts() throws Exception {
        String author = newUser();
        String fan = newUser();
        int id = createPost(author, "좋아요 테스트");

        mockMvc.perform(post("/api/posts/" + id + "/like").header("Authorization", bearer(fan)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.liked").value(true))
                .andExpect(jsonPath("$.likeCount").value(1));
        mockMvc.perform(get("/api/posts/" + id).header("Authorization", bearer(fan)))
                .andExpect(jsonPath("$.likedByMe").value(true)).andExpect(jsonPath("$.likeCount").value(1));
        mockMvc.perform(get("/api/posts/" + id).header("Authorization", bearer(author)))
                .andExpect(jsonPath("$.likedByMe").value(false));
        mockMvc.perform(post("/api/posts/" + id + "/like").header("Authorization", bearer(fan)))
                .andExpect(jsonPath("$.liked").value(false)).andExpect(jsonPath("$.likeCount").value(0));
    }

    @Test
    void commentsCreateListDeleteAndCount() throws Exception {
        String author = newUser();
        String commenter = newUser();
        int id = createPost(author, "댓글 테스트");

        MvcResult c = mockMvc.perform(post("/api/posts/" + id + "/comments").header("Authorization", bearer(commenter))
                .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"좋은 글이네요\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.authorNickname").isNotEmpty()).andReturn();
        int commentId = JsonPath.read(c.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/posts/" + id + "/comments")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("좋은 글이네요"));
        mockMvc.perform(get("/api/posts/" + id)).andExpect(jsonPath("$.commentCount").value(1));

        mockMvc.perform(delete("/api/comments/" + commentId).header("Authorization", bearer(author)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/comments/" + commentId).header("Authorization", bearer(commenter)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/posts/" + id + "/comments")).andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/posts/" + id)).andExpect(jsonPath("$.commentCount").value(0));
    }

    @Test
    void deletePostByAuthorAndByAdmin() throws Exception {
        String author = newUser();
        String other = newUser();
        String admin = newAdmin();
        int mine = createPost(author, "내가 지울 글");
        int others = createPost(author, "관리자가 지울 글");

        mockMvc.perform(delete("/api/posts/" + mine).header("Authorization", bearer(other)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/posts/" + mine).header("Authorization", bearer(author)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/posts/" + mine)).andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/posts/" + others).header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/posts/" + others)).andExpect(status().isNotFound());
    }

    @Test
    void reportIsIdempotent() throws Exception {
        String author = newUser();
        String reporter = newUser();
        int id = createPost(author, "신고 테스트");
        String body = "{\"reason\":\"광고성 글입니다\"}";

        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/posts/" + id + "/report").header("Authorization", bearer(reporter))
                    .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isNoContent());
        }
        Integer count = jdbc.queryForObject("select count(*) from post_reports where post_id = ?", Integer.class, id);
        assertThat(count).isEqualTo(1);
        mockMvc.perform(post("/api/posts/" + id + "/report").header("Authorization", bearer(reporter))
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"\"}")).andExpect(status().isBadRequest());
    }

    @Test
    void paginationAndPopularSort() throws Exception {
        String author = newUser();
        String fan = newUser();
        int first = createPost(author, "페이지 A");
        createPost(author, "페이지 B");
        createPost(author, "페이지 C");
        mockMvc.perform(post("/api/posts/" + first + "/like").header("Authorization", bearer(fan)));

        mockMvc.perform(get("/api/posts").param("size", "2").param("page", "0"))
                .andExpect(jsonPath("$.items.length()").value(2)).andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalPages").isNumber());
        // 좋아요를 받은 글이 인기순 맨 앞에 온다.
        mockMvc.perform(get("/api/posts").param("sort", "popular").param("size", "50"))
                .andExpect(jsonPath("$.items[0].likeCount").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }
}
