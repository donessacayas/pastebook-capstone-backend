package com.pointwest.pastebook.pastebook_backend.services;

import com.pointwest.pastebook.pastebook_backend.config.JwtToken;
import com.pointwest.pastebook.pastebook_backend.models.LikedPost;
import com.pointwest.pastebook.pastebook_backend.models.Notification;
import com.pointwest.pastebook.pastebook_backend.models.Post;
import com.pointwest.pastebook.pastebook_backend.models.User;
import com.pointwest.pastebook.pastebook_backend.repositories.LikedPostRepository;
import com.pointwest.pastebook.pastebook_backend.repositories.NotificationRepository;
import com.pointwest.pastebook.pastebook_backend.repositories.PostRepository;
import com.pointwest.pastebook.pastebook_backend.repositories.UserRepository;
import com.pointwest.pastebook.pastebook_backend.sockets.SocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Optional;

@Service
public class LikedPostImpl implements LikedPostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private LikedPostRepository likedPostRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtToken jwtToken;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SocketHandler socketHandler;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    @Override
    public LikedPost likePost(Long postId, String token) throws IOException {

        Optional<Post> postToLike = postRepository.findById(postId);
        Long authenticatedId = Long.parseLong(jwtToken.getIdFromToken(token));
        User user = userRepository.findById(authenticatedId).get();

        if(postToLike.isPresent()){
            LikedPost commenceLike = new LikedPost();
            commenceLike.setPost(postToLike.get());
            commenceLike.setUser(user);
            postToLike.get().getLikes().add(commenceLike);
            postRepository.save(postToLike.get());
           // likedPostRepository.save(commenceLike);


            //Add notifications
            Notification notification = new Notification();
            notification.setPost(postToLike.get());
            notification.setContent("liked your post");
            //notification.setDatetimeCreated(dateFormat.format(Timestamp.from(Instant.now())));
            notification.setDatetimeCreated(new Timestamp(System.currentTimeMillis()));
            notification.setSender(user);
            notification.setReadStatus(false);
            notificationRepository.save(notification);
            // Notify User
            socketHandler.notifyUser(postToLike.get().getUser().getId());
              return likedPostRepository.save(commenceLike);
        } else {
            throw new RuntimeException("error");
        }


    }

    @Override
    public void unlikePost(Long postId, String token) {
        Optional<Post> postToUnLike = postRepository.findById(postId);
        if(postToUnLike.isPresent()){
            Long authenticatedId = Long.parseLong(jwtToken.getIdFromToken(token));
            User user = userRepository.findById(authenticatedId).get();

            LikedPost unlike = likedPostRepository.getLikePostToUnlike(postToUnLike.get().getId(), user.getId());

            postToUnLike.get().getLikes().remove(unlike);
            postRepository.save(postToUnLike.get());
            likedPostRepository.delete(unlike);
        }
    }

    @Override
    public Integer getLikes(Long postId) {
        Post post = postRepository.findById(postId).get();
        return post.getLikes().size();
    }

}