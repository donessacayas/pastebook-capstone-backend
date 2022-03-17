package com.pointwest.pastebook.pastebook_backend.services;

import com.pointwest.pastebook.pastebook_backend.models.User;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface UserService {
    User createUser(User user);

    User getUserById(Long id);
    // update user
    User updateUserCredentials(User user, Long id);

    User updateUserPersonalDetails(User user, Long id, String token);

    ResponseEntity updateAboutMe(String aboutMe, Long id, String token);


    // For getting
    User getUser(Long id, String token);
    ResponseEntity getProfile(String profileUrl, String token);

    // search user
    ResponseEntity searchUser(String searchTerm, String stringToken);

    Optional<User> findByEmail(String email);
    Optional<User> findByMobile(String mobile);

    // CODES FOR TESTING
    ResponseEntity getUsersTest();

    // get user
    ResponseEntity getUserTest(Long id);



}