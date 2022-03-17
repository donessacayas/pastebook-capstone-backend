package com.pointwest.pastebook.pastebook_backend.repositories;

import com.pointwest.pastebook.pastebook_backend.models.Friend;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRepository extends CrudRepository<Friend, Object> {
    @Query(value = "SELECT CASE WHEN friends.recipient_id = ?1 THEN requester_id" +
            "ELSE recipient_id" +
            "END as myFriendID" +
            "FROM friends" +
            "WHERE recipient_id = ?1 OR requester_id = ?1" +
            "GROUP BY LEAST(recipient_id,requester_id), GREATEST(recipient_id,requester_id)"
            , nativeQuery = true)
    List<Long> friendsIds(Long userId);
}
