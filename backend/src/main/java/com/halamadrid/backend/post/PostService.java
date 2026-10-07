package com.halamadrid.backend.post;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.halamadrid.backend.common.ApiException;
import com.halamadrid.backend.common.CurrentUser;
import com.halamadrid.backend.common.PageResponse;
import com.halamadrid.backend.post.PostDtos.LikeResponse;
import com.halamadrid.backend.post.PostDtos.PostDetail;
import com.halamadrid.backend.post.PostDtos.PostRequest;
import com.halamadrid.backend.post.PostDtos.PostSummary;
import com.halamadrid.backend.user.User;
import com.halamadrid.backend.user.UserRepository;

@Service
public class PostService {

    private static final int MAX_PAGE_SIZE = 50;

    private final PostRepository postRepository;
    private final PostLikeRepository likeRepository;
    private final PostReportRepository reportRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, PostLikeRepository likeRepository,
            PostReportRepository reportRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<PostSummary> list(int page, int size, String sort) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Sort order = "popular".equals(sort)
                ? Sort.by(Sort.Order.desc("likeCount"), Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
                : Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        return PageResponse.of(postRepository.findByDeletedFalse(PageRequest.of(Math.max(page, 0), safeSize, order)),
                PostSummary::from);
    }

    /** 상세 조회. 조회수를 1 올린다. */
    @Transactional
    public PostDetail get(Long postId, CurrentUser viewer) {
        findPost(postId);
        postRepository.incrementViews(postId);
        Post post = findPost(postId);
        boolean liked = viewer.id() != null && likeRepository.exists(postId, viewer.id());
        return PostDetail.from(post, liked);
    }

    @Transactional
    public PostDetail create(CurrentUser user, PostRequest request) {
        User author = findUser(user.requireId());
        Post saved = postRepository.save(new Post(author, request.title().trim(), request.content().trim()));
        return PostDetail.from(saved, false);
    }

    @Transactional
    public PostDetail update(CurrentUser user, Long postId, PostRequest request) {
        Post post = findPost(postId);
        if (!post.getAuthor().getId().equals(user.requireId())) {
            throw forbidden("본인이 쓴 글만 수정할 수 있습니다.");
        }
        post.update(request.title().trim(), request.content().trim());
        return PostDetail.from(post, likeRepository.exists(postId, user.id()));
    }

    @Transactional
    public void delete(CurrentUser user, Long postId) {
        Post post = findPost(postId);
        if (!user.admin() && !post.getAuthor().getId().equals(user.requireId())) {
            throw forbidden("본인이 쓴 글만 삭제할 수 있습니다.");
        }
        post.markDeleted();
    }

    /** 좋아요 토글. 이미 눌렀다면 취소한다. */
    @Transactional
    public LikeResponse toggleLike(CurrentUser user, Long postId) {
        Long userId = user.requireId();
        Post post = findPost(postId);
        boolean liked;
        if (likeRepository.exists(postId, userId)) {
            likeRepository.delete(postId, userId);
            postRepository.addLikes(postId, -1);
            liked = false;
        } else {
            try {
                likeRepository.saveAndFlush(new PostLike(post, findUser(userId)));
                postRepository.addLikes(postId, 1);
            } catch (DataIntegrityViolationException e) {
                // 거의 동시에 두 번 눌린 경우: 이미 반영된 것으로 본다.
            }
            liked = true;
        }
        return new LikeResponse(liked, findPost(postId).getLikeCount());
    }

    /** 신고. 같은 사람이 같은 글을 여러 번 신고해도 한 번만 저장한다. */
    @Transactional
    public void report(CurrentUser user, Long postId, String reason) {
        Long userId = user.requireId();
        Post post = findPost(postId);
        if (!reportRepository.exists(postId, userId)) {
            reportRepository.save(new PostReport(post, findUser(userId), reason.trim()));
        }
    }

    private Post findPost(Long id) {
        return postRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "글을 찾을 수 없습니다."));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
    }

    private ApiException forbidden(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", message);
    }
}
