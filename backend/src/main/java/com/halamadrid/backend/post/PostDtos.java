package com.halamadrid.backend.post;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class PostDtos {

    private PostDtos() {
    }

    public record PostRequest(
            @NotBlank(message = "제목을 입력해 주세요.")
            @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
            String title,

            @NotBlank(message = "내용을 입력해 주세요.")
            @Size(max = 5000, message = "내용은 5000자 이하여야 합니다.")
            String content) {
    }

    public record ReportRequest(
            @NotBlank(message = "신고 사유를 입력해 주세요.")
            @Size(max = 200, message = "신고 사유는 200자 이하여야 합니다.")
            String reason) {
    }

    public record PostSummary(Long id, String title, String authorNickname, Instant createdAt,
            int viewCount, int likeCount, int commentCount) {

        static PostSummary from(Post p) {
            return new PostSummary(p.getId(), p.getTitle(), p.getAuthor().getNickname(), p.getCreatedAt(),
                    p.getViewCount(), p.getLikeCount(), p.getCommentCount());
        }
    }

    public record PostDetail(Long id, String title, String content, Long authorId, String authorNickname,
            Instant createdAt, Instant updatedAt, int viewCount, int likeCount, int commentCount, boolean likedByMe) {

        static PostDetail from(Post p, boolean likedByMe) {
            return new PostDetail(p.getId(), p.getTitle(), p.getContent(), p.getAuthor().getId(),
                    p.getAuthor().getNickname(), p.getCreatedAt(), p.getUpdatedAt(), p.getViewCount(),
                    p.getLikeCount(), p.getCommentCount(), likedByMe);
        }
    }

    public record LikeResponse(boolean liked, int likeCount) {
    }
}
