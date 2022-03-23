package com.pointwest.pastebook.pastebook_backend.services;

import com.pointwest.pastebook.pastebook_backend.models.LikedPost;
import com.pointwest.pastebook.pastebook_backend.models.Post;
import com.pointwest.pastebook.pastebook_backend.models.User;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

public interface LikedPostService {



    void likePost(Long postId, String token) throws IOException;


    Integer getLikes(Long postId);

    void unlikePost(Long postId, String token);

    //Iterable<Post> getTaggedPosts(String stringToken);
    //Iterable<Post> getAllPostRelatedToUser(Long userId, String stringToken);

}