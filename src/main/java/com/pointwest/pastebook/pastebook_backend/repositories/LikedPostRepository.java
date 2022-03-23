package com.pointwest.pastebook.pastebook_backend.repositories;

import com.pointwest.pastebook.pastebook_backend.models.LikedPost;
import com.pointwest.pastebook.pastebook_backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikedPostRepository extends JpaRepository<LikedPost, Long> {
    @Query(value = "SELECT * FROM liked_posts WHERE post_id= ?1 AND user_id = ?2",
            nativeQuery = true)
    LikedPost getLikePostToUnlike(Long post_id, Long user_id);

    LikedPost findByUser_Id(Long id);
}