package com.pointwest.pastebook.pastebook_backend.controllers;

import com.pointwest.pastebook.pastebook_backend.models.User;
import com.pointwest.pastebook.pastebook_backend.models.dto.UserDto;
import com.pointwest.pastebook.pastebook_backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping(value = "/api")
public class UserController {
  
  @Autowired private UserService userService;

  @RequestMapping(value = "/test", method = RequestMethod.GET)
  public ResponseEntity<Object> testConnection() {
    return new ResponseEntity<Object>("Connection alright!", HttpStatus.OK);
  }

  @PostMapping("/users/register")
  public ResponseEntity<User> createUser(@RequestBody UserDto userDto) {
    User user = new User();
    user.setFirstName(userDto.getFirstName());
    user.setLastName(userDto.getLastName());
    user.setBirthday(userDto.getBirthday());
    user.setEmail(userDto.getEmail());
    user.setMobileNumber(userDto.getMobileNumber());
    user.setGender(userDto.getGender());
    // default values
    user.setOnline(false);
    user.setEnabled(false);

    String password = userDto.getPassword();
    String encodedPassword = new BCryptPasswordEncoder().encode(password);
    user.setPassword(encodedPassword);

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    LocalDateTime now = LocalDateTime.now();
    user.setDatetimeCreated(dtf.format(now));

    user.setAboutMe("");
    user.setProfileUrl("");
    user.setProfilePic("");

    URI uri =
        URI.create(
            ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/users/register")
                .toUriString());
    return ResponseEntity.created(uri).body(userService.createUser(user));
  }

  @GetMapping("/users/{userId}")
  public ResponseEntity<User> getUserById(@PathVariable Long userId) {
    return ResponseEntity.ok().body(userService.getUserById(userId));
  }

  @RequestMapping(value = "/users/details/{userid}", method = RequestMethod.GET)
  public ResponseEntity<Object> getAccountDetails(
      @PathVariable Long userid, @RequestHeader(value = "Authorization") String stringToken) {
    return new ResponseEntity<>(userService.getUser(userid, stringToken), HttpStatus.OK);
  }

  @RequestMapping(value = "/users/profile/{profileUrl}", method = RequestMethod.GET)
  public ResponseEntity<Object> getProfile(
      @PathVariable String profileUrl, @RequestHeader(value = "Authorization") String stringToken) {
    return userService.getProfile(profileUrl, stringToken);
  }

  @PutMapping("/users/details/{userId}")
  public ResponseEntity<Object> updateUserPersonalDetails(
      @RequestBody UserDto userDto,
      @PathVariable Long userId,
      @RequestHeader(value = "Authorization") String stringToken) {

    User user = new User();
    user.setFirstName(userDto.getFirstName());
    user.setLastName(userDto.getLastName());
    user.setBirthday(userDto.getBirthday());
    user.setGender(userDto.getGender());
    return ResponseEntity.ok()
        .body(userService.updateUserPersonalDetails(user, userId, stringToken));
  }

  // search user
  @RequestMapping(value = "/users/search/{searchTerm}", method = RequestMethod.GET)
  public ResponseEntity<Object> searchUser(
      @PathVariable String searchTerm, @RequestHeader(value = "Authorization") String stringToken) {
    return userService.searchUser(searchTerm);
  }

  @RequestMapping(value = "/users/aboutme/{userId}", method = RequestMethod.PUT)
  public ResponseEntity<Object> searchUser(
      @RequestBody Map<String, String> body,
      @PathVariable Long userId,
      @RequestHeader(value = "Authorization") String stringToken) {

    return userService.updateAboutMe(body.get("aboutme"), userId, stringToken);
  }
  
  // FOR TESTING CODES
  // get users
  @RequestMapping(value = "/users/test", method = RequestMethod.GET)
  public ResponseEntity<Object> getUsersTest() {
    return userService.getUsersTest();
  }

  // get user
  //    @RequestMapping(value="/users/{userid}/test", method = RequestMethod.GET)
  //    public ResponseEntity<Object> getUserTest(@PathVariable Long userid) {
  //        return userService.getUserTest(userid);
  //    }
}