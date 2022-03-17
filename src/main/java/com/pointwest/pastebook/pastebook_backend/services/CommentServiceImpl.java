package com.pointwest.pastebook.pastebook_backend.services;

import com.pointwest.pastebook.pastebook_backend.config.JwtToken;
import com.pointwest.pastebook.pastebook_backend.models.*;
import com.pointwest.pastebook.pastebook_backend.repositories.*;
import com.pointwest.pastebook.pastebook_backend.sockets.SocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService{
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtToken jwtToken;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SocketHandler socketHandler;

    @Override
    public Comment commentPost(Long postId, String content, String token) throws IOException {
        Optional<Post> postToComment = postRepository.findById(postId);
        if(postToComment.isPresent()){
            Comment comment = new Comment();
            comment.setPost(postToComment.get());
            Post post = postToComment.get();
            Long authenticatedId = Long.parseLong(jwtToken.getIdFromToken(token));
            User user = userRepository.findById(authenticatedId).get();
            comment.setUser(user);
            comment.setContent(content);
            post.getComments().add(comment);
            postRepository.save(post);

            Notification notification = new Notification();
            notification.setPost(post);
            notification.setContent("commented on your post");
            //notification.setDatetimeCreated(dateFormat.format(Timestamp.from(Instant.now())));
            notification.setDatetimeCreated(new Timestamp(System.currentTimeMillis()));
            notification.setSender(user);
            notification.setReadStatus(false);
            notificationRepository.save(notification);
            // Notify User
            socketHandler.notifyUser(post.getUser().getId());
            return commentRepository.save(comment);
        }else{
            throw new RuntimeException("An error occurred");
        }
    }

    @Override
    public ResponseEntity removeComment(Long commentId, String token) {

        Optional<Comment> commentTOEdit = commentRepository.findById(commentId);
        if(commentTOEdit.isPresent()) {
            Long authenticatedId = Long.parseLong(jwtToken.getIdFromToken(token));
            User user = userRepository.findById(authenticatedId).get();

            //Comment commentToRemove = commentRepository.getCommentObject(postToRemoveCommentFrom.get().getId(), user.getId());
            Optional<Post> postToUpdateComment = postRepository.findById(commentTOEdit.get().getPost().getId());
            postToUpdateComment.get().getComments().remove(commentTOEdit.get());
            postRepository.save(postToUpdateComment.get());
            commentRepository.delete(commentTOEdit.get());
            return new ResponseEntity("Remove comment on post", HttpStatus.OK);
        }else{
            return new ResponseEntity("Comment not found!", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity editComment(Long commentId, String content, String token) {

        Optional<Comment> commentTOEdit = commentRepository.findById(commentId);
        if(commentTOEdit.isPresent()) {
            Long authenticatedId = Long.parseLong(jwtToken.getIdFromToken(token));
            User user = userRepository.findById(authenticatedId).get();

            //Comment commentToRemove = commentRepository.getCommentObject(postToRemoveCommentFrom.get().getId(), user.getId());
            Optional<Post> postToUpdateComment = postRepository.findById(commentTOEdit.get().getPost().getId());
            postToUpdateComment.get().getComments().remove(commentTOEdit.get());
            commentTOEdit.get().setContent(content);
            postToUpdateComment.get().getComments().add(commentTOEdit.get());
            postRepository.save(postToUpdateComment.get());
            commentRepository.save(commentTOEdit.get());
            return new ResponseEntity("Edit comment on post", HttpStatus.OK);
        }else{
            return new ResponseEntity("Comment not found!", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public Iterable<Comment> getCommentsInPost(Long postId) {
        return postRepository.findById(postId).get().getComments();
    }
}
