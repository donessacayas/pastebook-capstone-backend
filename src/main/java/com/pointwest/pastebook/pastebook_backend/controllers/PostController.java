package com.pointwest.pastebook.pastebook_backend.controllers;

import com.pointwest.pastebook.pastebook_backend.models.Image;
import com.pointwest.pastebook.pastebook_backend.models.Post;
import com.pointwest.pastebook.pastebook_backend.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/posts")
public class PostController {
  @Autowired private PostService postService;

  @PostMapping
  public ResponseEntity<Object> createPost(
      @RequestParam(value = "content", required = false) String content,
      @RequestParam(value = "tagged", required = false) Long id,
      @RequestPart(value = "image", required = false) MultipartFile imageFile,
      @RequestHeader("Authorization") String token)
      throws IOException {
    Post post = new Post();
    if (imageFile != null) {
      Image image =
          new Image(
              StringUtils.cleanPath(Objects.requireNonNull(imageFile.getOriginalFilename())),
              imageFile.getContentType(),
              imageFile.getBytes());
      post.setImage(image);
    }

    if (content != null) {
      post.setContent(content);
    }

    if (id != null) {
      return ResponseEntity.ok().body(postService.createPost(post, token, id));
    } else {
      return ResponseEntity.ok().body(postService.createPost(post, token));
    }
  }

  // GET /api/posts/{id}
  @GetMapping("/{postId}")
  public ResponseEntity<Post> getPostById(@PathVariable Long postId) {
    return ResponseEntity.ok().body(postService.getPostById(postId));
  }

  // GET /api/posts/user
  @GetMapping("/user")
  public ResponseEntity<Iterable<Post>> getPostFromUser(
      @RequestHeader(value = "Authorization") String token) {
    return ResponseEntity.ok().body(postService.getPostsFromUser(token));
  }

  // GET /api/posts
  @GetMapping
  public ResponseEntity<Iterable<Post>> getPosts() {
    return ResponseEntity.ok().body(postService.getAllPost());
  }

  // GET /api/posts
  @GetMapping("/friends/{userId}")
  public ResponseEntity<List<Post>> getPostWithFriends(
      @PathVariable Long userId,
      @RequestParam("page") Integer page,
      @RequestParam("size") Integer size) {
    return ResponseEntity.ok().body(postService.getPostOfFriends(userId, page, size));
  }

  @GetMapping("/pagination")
  public ResponseEntity<Object> getPostsWithPagination(
      @RequestParam(value = "page") Integer page, @RequestParam(value = "size") Integer size) {
    return ResponseEntity.ok().body(postService.getPostsPagination(page, size));
  }

  // UPDATE /api/posts/id
  @PutMapping("/{postId}")
  public ResponseEntity<Post> updatePosts(
      @PathVariable Long postId,
      @RequestParam(value = "image", required = false) MultipartFile imageFile,
      @RequestParam(value = "content") String content)
      throws IOException {
    Post post = new Post();
    if (imageFile != null) {
      Image image =
          new Image(
              StringUtils.cleanPath(Objects.requireNonNull(imageFile.getOriginalFilename())),
              imageFile.getContentType(),
              imageFile.getBytes());
      post.setImage(image);
    }

    if (content != null) {
      post.setContent(StringUtils.cleanPath(content));
    }

    return ResponseEntity.ok().body(postService.updatePost(postId, post));
  }

  // DELETE /api/posts
  @DeleteMapping("/{postId}")
  public ResponseEntity<Map<String, String>> deletePost(@PathVariable Long postId) {
    postService.deletePost(postId);
    Map<String, String> response = new HashMap<>();
    response.put("id", postId.toString());
    response.put("deleted", "true");
    return ResponseEntity.ok().body(response);
  }
}