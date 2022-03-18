package com.pointwest.pastebook.pastebook_backend.services;

import com.pointwest.pastebook.pastebook_backend.config.JwtToken;
import com.pointwest.pastebook.pastebook_backend.exceptions.EntityDuplicateException;
import com.pointwest.pastebook.pastebook_backend.exceptions.EntityNotFoundException;
import com.pointwest.pastebook.pastebook_backend.models.Image;
import com.pointwest.pastebook.pastebook_backend.models.User;
import com.pointwest.pastebook.pastebook_backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtToken jwtToken;

    @Autowired
    private JavaMailSender mailSender;


    @Override
    public User createUser(User user) {
        Optional<User> userDb = Optional.ofNullable(userRepository.findByEmail(user.getEmail()));
        if (userDb.isPresent()) {
            throw new EntityDuplicateException(User.class, "email", user.getEmail());
        }
        //When verified, change status to verify and set profileUrl
        userRepository.save(user);
        try {
            sendVerificationEmail(user, "http://localhost:4200");
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return userRepository.save(prodVerify(user));
    }

    private User prodVerify(User user) {
        Long id = userRepository.save(user).getId();
        user.setEnabled(true);
        user.setProfileUrl(user.getFirstName() + user.getLastName() + id);
        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(User.class, id));
    }

    @Override
    public User updateSecurityEmail(User user, Long id) {
        User userDb = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(User.class, id));
        userDb.setEmail(user.getEmail());
        return userRepository.save(userDb);
    }

    @Override
    public User updateSecurityPassword(User user, Long id) {
        User userDb = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(User.class, id));
        userDb.setPassword(user.getPassword());
        return userRepository.save(userDb);
    }

    @Override
    public User updatePersonalDetails(User user, Long id) {
        User userDb = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(User.class, id));
        userDb.setFirstName(user.getFirstName());
        userDb.setLastName(user.getLastName());
        userDb.setBirthday(user.getBirthday());
        userDb.setGender(user.getGender());
        userDb.setMobileNumber(user.getMobileNumber());

        String profileUrl = user.getFirstName() + user.getLastName() + id;
        userDb.setProfileUrl(profileUrl);
        return userRepository.save(userDb);
    }
//
//    public boolean verify(String verificationCode) {
//        User user = userRepository.findByVerificationCode(verificationCode);
//
//        if (user == null || user.isEnabled()) {
//            return false;
//        } else {
//            user.setVerificationCode(null);
//            user.setEnabled(true);
//            userRepository.save(user);
//
//            return true;
//        }
//
//    }
    // send verification email
    private void sendVerificationEmail(User user, String siteURL)
            throws MessagingException, UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String fromAddress = "grp4pastebook@gmail.com";
        String senderName = "Group 4 Pastebook";
        String subject = "Please verify your registration";
        String content = "Dear [[name]],<br>"
                + "Please click the link below to verify your registration:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>"
                + "Thank you,<br>"
                + "Your company name.";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", user.getFirstName());
        String verifyURL = siteURL + "/verify?code=" + user.getVerificationCode();

        content = content.replace("[[URL]]", verifyURL);

        helper.setText(content, true);

        mailSender.send(message);

        System.out.println("Email has been sent");
    }
    // get users
    public ResponseEntity getUsers() {
        return new ResponseEntity(userRepository.findAll(), HttpStatus.OK);
    }

    // get user
    public User getUser(Long id, String token) {
        return userRepository.findById(id).get();
    }

    @Override
    public ResponseEntity getProfile(String profileUrl, String token) {
        //token checker
        User user = userRepository.getUserProfileByUrl(profileUrl);
        if (user != null)
            return new ResponseEntity(user, HttpStatus.OK);
        else
            return new ResponseEntity("User not found!", HttpStatus.NOT_FOUND);
    }


//    @Override
//    public User updateUserPersonalDetails(User user, Long id, String token) {
//        User userDb = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(User.class, id));
//
//        String authenticatedEmail = jwtToken.getUsernameFromToken(token);
//        if (authenticatedEmail.equalsIgnoreCase(userDb.getEmail())) {
//            userDb.setFirstName(user.getFirstName());
//            userDb.setLastName(user.getLastName());
//            userDb.setGender(user.getGender());
//            userDb.setBirthday(user.getBirthday());
//            userDb.setProfileUrl(user.getFirstName()+user.getLastName() + id);
//            return userRepository.save(userDb);
//        } else {
//            throw  new RuntimeException("Unauthorized access");
//        }
//    }
//

    @Override
    public ResponseEntity updateAboutMe(String aboutMe, Long id, String token) {
        System.out.println("Id token is");
        System.out.println(jwtToken.getIdFromToken(token));
        Long authenticatedId = Long.parseLong(jwtToken.getIdFromToken(token));

        if (authenticatedId == id) {
            User user = userRepository.findById(authenticatedId).get();
            //check if empty later
            user.setAboutMe(aboutMe);
            userRepository.save(user);
            return new ResponseEntity("About me Updated", HttpStatus.OK);

        } else {
            return new ResponseEntity("You are not authorized to edit this aboutMe", HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public ResponseEntity uploadProfilePicture(Image image, String token) {
        //System.out.println("Upload image test");
        User user = userRepository.getById(Long.parseLong(jwtToken.getIdFromToken(token)));
        user.setImage(image);
        userRepository.save(user);
        return new ResponseEntity("Upload Success", HttpStatus.OK);
    }

    // search user
    public ResponseEntity searchUser(String searchTerm, String token) {
        ArrayList<User> searchedUsers = new ArrayList<>();
        ArrayList<String> searchedUsersUrl = new ArrayList<>();
        ArrayList<User> searchedUsersAlphabetical = new ArrayList<>();

        if (searchTerm.isBlank()) {
            return new ResponseEntity("No users found", HttpStatus.OK);
        } else {
            for (User existingUser : userRepository.findAll()) {
                // getting user by first name or last name
                if (existingUser.getFirstName().toLowerCase().contains(searchTerm.toLowerCase()) || existingUser.getLastName().toLowerCase().contains(searchTerm.toLowerCase())) {
                    searchedUsers.add(existingUser);
                    searchedUsersUrl.add(existingUser.getFirstName() + existingUser.getLastName() + existingUser.getId());
                }
            }

            // To arrange the users alphabetically, we first need to sort the 'searchedUsersUrl' array list
            // This is an array list arranged alphabetically
            Collections.sort(searchedUsersUrl);

            // Then, we need to associate the array list above with its corresponding object, where that object will be stored in an array list that we will display
            for (String stringUrl : searchedUsersUrl) {
                for (User user : searchedUsers) {
                    if (stringUrl.toLowerCase().equals(user.getFirstName().toLowerCase() + user.getLastName().toLowerCase() + user.getId())) {
                        searchedUsersAlphabetical.add(user);
                    }
                }
            }

            if (searchedUsers.size() == 0) {
                return new ResponseEntity("No users found", HttpStatus.OK);
            }
            return new ResponseEntity(searchedUsersAlphabetical, HttpStatus.OK);
        }
    }

    @Override
    public Optional<User> findByEmail(String username) {
        return Optional.ofNullable(userRepository.findByEmail(username));
    }

    @Override
    public Optional<User> findByMobile(String mobile) {
        System.out.println("Im in service :" + mobile);
        System.out.println(userRepository.getUserDetailsByMobile(mobile));
        System.out.println("test");
        return userRepository.getUserDetailsByMobile(mobile);
    }

    @Override
    public void setOnlineStatus(Long id) {
        System.out.println("Going online " + userRepository.findById(id));
        User user = userRepository.findById(id).get();
        user.setOnline(true);
        userRepository.save(user);
    }

    @Override
    public void setOfflineStatus(Long id) {
        System.out.println("Going offline " + userRepository.findById(id));
        User user = userRepository.findById(id).get();
        user.setOnline(false);
        userRepository.save(user);
    }


    // FOR TESTING CODES
    // get users
    public ResponseEntity getUsersTest() {
        return new ResponseEntity(userRepository.findAll(), HttpStatus.OK);
    }

    // get user
    public ResponseEntity getUserTest(Long id) {
        User user = userRepository.findById(id).get();
        return new ResponseEntity(user, HttpStatus.OK);
    }


}