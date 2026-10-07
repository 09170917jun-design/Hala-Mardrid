package com.halamadrid.backend.comment;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class CommentDtos {

    private CommentDtos() {
    }

    public record CommentRequest(
            @NotBlank(message = "댓글 내용을 입력해 주세요.")
            @Size(max = 500, message = "댓글은 500자 이하여야 합니다.")
            String content) {
    }

    public record CommentResponse(Long id, Long authorId, String authorNickname, String content, Instant createdAt) {

        static CommentResponse from(Comment c) {
            return new CommentResponse(c.getId(), c.getAuthor().getId(), c.getAuthor().getNickname(), c.getContent(),
                    c.getCreatedAt());
        }
    }
}
