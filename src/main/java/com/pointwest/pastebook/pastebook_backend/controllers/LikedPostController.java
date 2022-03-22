package com.pointwest.pastebook.pastebook_backend.controllers;

import com.pointwest.pastebook.pastebook_backend.models.LikedPost;
import com.pointwest.pastebook.pastebook_backend.services.LikedPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@CrossOrigin
@RequestMapping(value = "/api")
public class LikedPostController {
    @Autowired private LikedPostService likedPostService;

    @PostMapping("/like/{postId}")
    public ResponseEntity<Object> likePost(
            @PathVariable Long postId, @RequestHeader(value = "Authorization") String stringToken) throws IOException {
        likedPostService.likePost(postId, stringToken);
        return ResponseEntity.ok().body(true);

    }

    @RequestMapping(value = "/like/{postId}", method = RequestMethod.DELETE)
    public ResponseEntity<Object> unLikePost(
            @PathVariable Long postId, @RequestHeader(value = "Authorization") String stringToken) {
        likedPostService.unlikePost(postId, stringToken);
        return ResponseEntity.ok().body(true);
    }

    @GetMapping("/like/{postId}")
    public ResponseEntity<Integer> getLikes(@PathVariable Long postId) {
        return ResponseEntity.ok().body(likedPostService.getLikes(postId));
    }
}