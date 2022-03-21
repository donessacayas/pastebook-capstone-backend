package com.pointwest.pastebook.pastebook_backend.services;

import com.pointwest.pastebook.pastebook_backend.config.JwtToken;
import com.pointwest.pastebook.pastebook_backend.exceptions.EntityNotFoundException;
import com.pointwest.pastebook.pastebook_backend.models.Friend;
import com.pointwest.pastebook.pastebook_backend.models.Post;
import com.pointwest.pastebook.pastebook_backend.models.Tag;
import com.pointwest.pastebook.pastebook_backend.models.User;
import com.pointwest.pastebook.pastebook_backend.repositories.FriendRepository;
import com.pointwest.pastebook.pastebook_backend.repositories.PostRepository;
import com.pointwest.pastebook.pastebook_backend.repositories.TagRepository;
import com.pointwest.pastebook.pastebook_backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

  @Autowired private PostRepository postRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private TagRepository tagRepository;

  @Autowired private FriendRepository friendRepository;

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

    //    User userTobeTagged = userRepository.findById(id).get();
    //    Tag tag = new Tag();
    //    tag.setPost(post);
    //    tag.setUser(userTobeTagged);
    user.getPosts().add(post);
    //    userRepository.save(user);
    post.setUser(user);
    //    post.getTags().add(tag);
    return postRepository.save(post);
  }

  @Override
  public Post createPost(Post post, String token, Long id) {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    LocalDateTime now = LocalDateTime.now();
    post.setDatetimeCreated(dtf.format(now));

    Long authenticatedId = Long.parseLong(jwtToken.getIdFromToken(token));
    User user =
        userRepository
            .findById(authenticatedId)
            .orElseThrow(() -> new EntityNotFoundException(User.class, authenticatedId));

    User userDb = userRepository.findById(id).get();
    Tag tag = new Tag();
    tag.setUser(userDb);
    tag.setPost(post);
    post.setUser(user);
    post.getTags().add(tag);

    return postRepository.save(post);
  }

  @Override
  public Post getPostById(Long id) {
    return postRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(Post.class, id));
  }

  @Override
  public List<Post> getPostsFromUser(String stringToken) {
    User user = userRepository.findByEmail(jwtToken.getUsernameFromToken(stringToken));
    return user.getPosts();
  }

  @Override
  public Iterable<Post> getAllPost() {
    return postRepository.findAll();
  }

  @Override
  public List<Post> getPostOfFriends(Long id, Integer page, Integer size) {

    List<User> friendList = new ArrayList<>();
    List<Friend> friends = friendRepository.findAll();
    // looping through the first column in the friends table
    for (Friend friend : friends) {
      if (Objects.equals(friend.getRequester().getId(), id)) {
        friendList.add(friend.getRecipient());
      }
    }
    for (Friend friend : friends) {
      if (Objects.equals(friend.getRecipient().getId(), id)) {
        friendList.add(friend.getRequester());
      }
    }

    Collection<Long> ids = friendList.stream().map(User::getId).collect(Collectors.toList());
    System.out.println(ids);
    ids.add(id);
    return postRepository.findByUser_IdIn(
        ids, PageRequest.of(page, size).withSort(Sort.by(Sort.Direction.DESC, "id")));
  }

  @Override
  public Page<Post> getPostsPagination(Integer page, Integer size) {
    return postRepository.findAll(
        PageRequest.of(page, size).withSort(Sort.by(Sort.Direction.ASC, "id")));
  }

  @Override
  public Post updatePost(Long id, Post post) {
    Optional<Post> postDb = postRepository.findById(id);
    if (postDb.isPresent()) {
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