package com.pointwest.pastebook.pastebook_backend.services;

import com.pointwest.pastebook.pastebook_backend.config.JwtToken;
import com.pointwest.pastebook.pastebook_backend.exceptions.EntityNotFoundException;
import com.pointwest.pastebook.pastebook_backend.models.Post;
import com.pointwest.pastebook.pastebook_backend.models.User;
import com.pointwest.pastebook.pastebook_backend.repositories.PostRepository;
import com.pointwest.pastebook.pastebook_backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class PostServiceImpl implements PostService {

  @Autowired private PostRepository postRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private JwtToken jwtToken;

  @Override
  public Post createPost(Post post, String token) {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    LocalDateTime now = LocalDateTime.now();
    post.setDatetimeCreated(dtf.format(now));

    Long authenticatedId = Long.parseLong(jwtToken.getIdFromToken(token));
    User user =
        userRepository
            .findById(authenticatedId)
            .orElseThrow(() -> new EntityNotFoundException(User.class, authenticatedId));

    post.setUser(user);
    return postRepository.save(post);
  }

  @Override
  public Post getPostById(Long id) {
    return postRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(Post.class, id));
  }

  @Override
  public Iterable<Post> getPostsFromUser(String stringToken) {
    User user = userRepository.findByEmail(jwtToken.getUsernameFromToken(stringToken));
    return user.getPosts();
  }

  @Override
  public Iterable<Post> getAllPost() {
    return postRepository.findAll();
  }

  @Override
  public Page<Post> getPostsPagination(Integer page, Integer size) {
    return postRepository.findAll(PageRequest.of(page, size).withSort(Sort.by(Sort.Direction.DESC, "id")));
  }

  @Override
  public Post updatePost(Long id, Post post) {
    Optional<Post> postDb = postRepository.findById(id);
    if(postDb.isPresent()) {
      Post updatedPost = postDb.get();
      updatedPost.setImage(post.getImage());
      updatedPost.setContent(post.getContent());
      return updatedPost;
    } else {
      throw new EntityNotFoundException(Post.class, id);
    }
  }

  @Override
  public void deletePost(Long id) {
    Optional<Post> postDb = postRepository.findById(id);
    if (postDb.isPresent()) {
      postRepository.deleteById(id);
    } else {
      throw new EntityNotFoundException(Post.class, id);
    }
  }
}