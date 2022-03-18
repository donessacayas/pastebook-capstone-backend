package com.pointwest.pastebook.pastebook_backend.repositories;

import com.pointwest.pastebook.pastebook_backend.models.Post;
import com.pointwest.pastebook.pastebook_backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
//    @Query(value = "SELECT * FROM POSTS WHERE `user.id` IN (SELECT  = ?1",
//            countQuery = "SELECT count(*) FROM POSTS WHERE  = ?1",
//            nativeQuery = true)
    List<Post> findByUser_IdIn(Collection<Long> id);
}