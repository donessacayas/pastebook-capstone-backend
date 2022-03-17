package com.pointwest.pastebook.pastebook_backend.services;

import com.pointwest.pastebook.pastebook_backend.models.Friend;
import com.pointwest.pastebook.pastebook_backend.models.FriendRequest;
import com.pointwest.pastebook.pastebook_backend.models.User;
import com.pointwest.pastebook.pastebook_backend.repositories.FriendRepository;
import com.pointwest.pastebook.pastebook_backend.repositories.FriendRequestRepository;
import com.pointwest.pastebook.pastebook_backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

@Service
public class FriendServiceImpl implements FriendService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    // accepting friend
    @Override
    public ResponseEntity acceptFriend(HashMap<String, Object> friendMap) {
        // need to add condition for the case where person A and person B BOTH sent a friend request to each other, but naunang i accept ni person A si person B (but still, the friend request person B has from person A is still existing), pero we can just add a conditional na kapag in-accept ito ni person B, sasabihin ("You're already friends [with Person A])

        String requesterIdStr = friendMap.get("requesterId").toString();
        String recipientIdStr = friendMap.get("recipientId").toString();

        Long requesterId = Long.parseLong(requesterIdStr);
        Long recipientId = Long.parseLong(recipientIdStr);

        for (Friend friend : friendRepository.findAll()) {
            if ((friend.getRequester().getId() == requesterId && friend.getRecipient().getId() == recipientId) || (friend.getRequester().getId() == recipientId && friend.getRecipient().getId() == requesterId)) {

                // add a code here that deletes the record in the friend_requests table where the friend request is already INVALID since both of the users are already friends! (basically deleting the record in the database coming from the person na nahuli sa pag accept ng friend request)
                for (FriendRequest friendRequest : friendRequestRepository.findAll()) {
                    if ((friendRequest.getSender().getId() == friend.getRequester().getId() && friendRequest.getReceiver().getId() == friend.getRecipient().getId()) || (friendRequest.getSender().getId() == friend.getRecipient().getId() && friendRequest.getReceiver().getId() == friend.getRequester().getId())) {
                        friendRequestRepository.deleteById(friendRequest.getId());
                    }
                }
                return new ResponseEntity("You're already friends!", HttpStatus.OK);
            }
        }
        // IMPORTANT: the code above ensures that for every record in the friends table, yung mga ids dun ay pwedeng pag baliktarin, e.g. (1, 2) is the same as (2, 1), so di na magcrecreate ng new record na (2, 1)

        // for now, we need the recipientId since we still don't have the token. this will serve as the 'logged-in acc'

        // grabbing the record in the FriendRequest Model with the same requesterId and recipientId
        // since we're going to use the FriendRequest model, make sure that we create a conditional statement that utilizes that model. To do this, access the friend_requests table, and then loop through all of its records, then add the necessary conditional statement
        for (FriendRequest friendRequest : friendRequestRepository.findAll()) {
            if (friendRequest.getSender().getId() == requesterId && friendRequest.getReceiver().getId() == recipientId) {

                // creating the requester and recipient (User) objects
                User requester = userRepository.findById(requesterId).get();
                User recipient = userRepository.findById(recipientId).get();

                // getting 'Date' object and converting it to string
                LocalDateTime dateObject = LocalDateTime.now();
                DateTimeFormatter formatDateObj = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
                String formattedDate = dateObject.format(formatDateObj);

                // creating the Friend object based on the requester and recipient (User) objects
                Friend friend = new Friend(true, formattedDate, requester, recipient);

                friendRepository.save(friend);

                // deleting the record with the very same requesterId and recipientId in the friend_requests table
                friendRequestRepository.deleteById(friendRequest.getId());

                // also deleting the record (if sabay na nag-add yung 2 people sa isa't isa)
                for (FriendRequest request : friendRequestRepository.findAll()) {
                    if (request.getSender().getId() == recipientId && request.getReceiver().getId() == requesterId) {
                        friendRequestRepository.deleteById(request.getId());
                    }
                }

                return new ResponseEntity("Accepted the friend request!", HttpStatus.CREATED);
            }
        }
        return new ResponseEntity("Exception message", HttpStatus.CREATED);
    }

    // deleting friend
    @Override
    public ResponseEntity deleteFriend(Long requesterId, Long recipientId) {

        for (Friend friend : friendRepository.findAll()) {
            if ((friend.getRequester().getId() == requesterId && friend.getRecipient().getId() == recipientId) || (friend.getRequester().getId() == recipientId && friend.getRecipient().getId() == requesterId)) {
                friendRepository.deleteById(friend.getId());
                return new ResponseEntity("Delete the friend successfully!", HttpStatus.OK);
            }
        }
        return new ResponseEntity("Exception message", HttpStatus.CREATED);
    }

    // VERY IMPORTANT NOTE: the friends table is actually really complex because 'friends' is bi-directional! we just added a conditional statement for all the methods here to cover this bi-directional relationship

    // get all friends given a particular page id
    @Override
    public ResponseEntity getFriends(Long pageId) {
        ArrayList<User> friendListPart1 = new ArrayList<>();
        ArrayList<User> friendListPart2 = new ArrayList<>();

        // looping through the first column in the friends table
        for (Friend friend : friendRepository.findAll()) {
            if (friend.getRequester().getId() == pageId) {
                // get the corresponding friend.getRecipient and put it into an array list
                friendListPart1.add(friend.getRecipient());
            }
        }

        // looping through the second column in the friends table
        for (Friend friend : friendRepository.findAll()) {
            if (friend.getRecipient().getId() == pageId) {
                // get the corresponding friend.getRequester and put it into an array list
                friendListPart1.add(friend.getRequester());
            }
        }

        // combining the two array lists together
        friendListPart1.addAll(friendListPart2);

        return new ResponseEntity(friendListPart1, HttpStatus.OK);

    }



    // get one friend
    @Override
    public ResponseEntity getOneFriend(Long requesterId, Long recipientId) {
        for (Friend friend : friendRepository.findAll()) {
            if ((requesterId == friend.getRequester().getId() && recipientId == friend.getRecipient().getId()) || (requesterId == friend.getRecipient().getId() && requesterId == friend.getRequester().getId())) {
                Optional<Friend> optionalFriend = Optional.of(friend);
                System.out.println(optionalFriend);
                return new ResponseEntity(optionalFriend, HttpStatus.OK);
            }
        }
        Optional<Friend> noFriend = Optional.of(new Friend());
        return new ResponseEntity(noFriend, HttpStatus.OK);
    }


}