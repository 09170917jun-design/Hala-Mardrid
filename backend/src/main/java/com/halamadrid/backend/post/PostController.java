package com.halamadrid.backend.post;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.halamadrid.backend.common.CurrentUser;
import com.halamadrid.backend.common.PageResponse;
import com.halamadrid.backend.post.PostDtos.LikeResponse;
import com.halamadrid.backend.post.PostDtos.PostDetail;
import com.halamadrid.backend.post.PostDtos.PostRequest;
import com.halamadrid.backend.post.PostDtos.PostSummary;
import com.halamadrid.backend.post.PostDtos.ReportRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public PageResponse<PostSummary> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sort) {
        return postService.list(page, size, sort);
    }

    @GetMapping("/{id}")
    public PostDetail get(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return postService.get(id, CurrentUser.from(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostDetail create(@Valid @RequestBody PostRequest request, @AuthenticationPrincipal Jwt jwt) {
        return postService.create(CurrentUser.from(jwt), request);
    }

    @PutMapping("/{id}")
    public PostDetail update(@PathVariable Long id, @Valid @RequestBody PostRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return postService.update(CurrentUser.from(jwt), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        postService.delete(CurrentUser.from(jwt), id);
    }

    @PostMapping("/{id}/like")
    public LikeResponse like(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return postService.toggleLike(CurrentUser.from(jwt), id);
    }

    @PostMapping("/{id}/report")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void report(@PathVariable Long id, @Valid @RequestBody ReportRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        postService.report(CurrentUser.from(jwt), id, request.reason());
    }
}
