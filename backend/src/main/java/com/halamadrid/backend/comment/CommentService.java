package com.halamadrid.backend.comment;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.halamadrid.backend.comment.CommentDtos.CommentRequest;
import com.halamadrid.backend.comment.CommentDtos.CommentResponse;
import com.halamadrid.backend.common.ApiException;
import com.halamadrid.backend.common.CurrentUser;
import com.halamadrid.backend.post.Post;
import com.halamadrid.backend.post.PostRepository;
import com.halamadrid.backend.user.User;
import com.halamadrid.backend.user.UserRepository;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository,
            UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> list(Long postId) {
        findPost(postId);
        return commentRepository.findByPostIdAndDeletedFalseOrderByIdAsc(postId).stream()
                .map(CommentResponse::from).toList();
    }

    @Transactional
    public CommentResponse create(CurrentUser user, Long postId, CommentRequest request) {
        Long userId = user.requireId();
        Post post = findPost(postId);
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
        Comment saved = commentRepository.save(new Comment(post, author, request.content().trim()));
        postRepository.addComments(postId, 1);
        return CommentResponse.from(saved);
    }

    @Transactional
    public void delete(CurrentUser user, Long commentId) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다."));
        if (!user.admin() && !comment.getAuthor().getId().equals(user.requireId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "본인이 쓴 댓글만 삭제할 수 있습니다.");
        }
        comment.markDeleted();
        postRepository.addComments(comment.getPost().getId(), -1);
    }

    private Post findPost(Long id) {
        return postRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "글을 찾을 수 없습니다."));
    }
}
