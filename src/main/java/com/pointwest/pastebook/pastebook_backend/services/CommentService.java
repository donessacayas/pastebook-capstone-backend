package com.pointwest.pastebook.pastebook_backend.services;

import com.pointwest.pastebook.pastebook_backend.models.Comment;
import com.pointwest.pastebook.pastebook_backend.models.Post;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface CommentService {
    Comment commentPost(Long postId, String content, String token) throws IOException;
    ResponseEntity removeComment(Long postId, String token);

    ResponseEntity editComment(Long commentId, String content, String token);

    Iterable<Comment> getCommentsInPost(Long postId);

    //Iterable<Post> getCommentsFromPost(Long postId, String stringToken);
}
