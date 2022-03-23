package com.pointwest.pastebook.pastebook_backend.sockets;

import com.google.gson.Gson;
import com.pointwest.pastebook.pastebook_backend.repositories.FriendRepository;
import com.pointwest.pastebook.pastebook_backend.services.FriendService;
import com.pointwest.pastebook.pastebook_backend.services.UserService;
import com.pointwest.pastebook.pastebook_backend.services.UserServiceImpl;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
@Component
@Configurable
public class SocketHandler extends TextWebSocketHandler {
    List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    Map<String, WebSocketSession> id_sessions = new HashMap<>();
    int messagecount = 0;

    @Autowired
    private UserService userService;
    @Autowired
    private FriendRepository friendRepository;

    //UserServiceImpl userService = new UserServiceImpl();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws InterruptedException, IOException {

//        System.out.println("Message from client");
        Map value = new Gson().fromJson(message.getPayload(), Map.class);

        if(value.get("trigger") == null){
            id_sessions.put((String) value.get("user_id"),session);
            System.out.println("user id " + value.get("user_id") + " has connected");
            userService.setOnlineStatus(Long.parseLong((String) value.get("user_id")));
        }else{
            //Not needed anymore
            // Error handler
        }

    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if(!sessions.contains(session)){
            sessions.add(session);
        }

       // System.out.println("Active sessions: "+ sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // do something on connection closed
        sessions.remove(session);
        String id = getIdFromConnections(session);
        System.out.println(id);
        if(id!=null){
            userService.setOfflineStatus(Long.parseLong(id));
           // System.out.println("user id " + id + " has disconnected");
        }

    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        // handle binary message
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        // hanedle transport error
    }


    public void notifyMyFriends(Long userId) throws IOException {
        //System.out.println("Connections: " + id_sessions.size() );
        List<Long> userIds = friendRepository.friendsIds(userId);
        for (Long friendsId:userIds) {
            for (Map.Entry<String, WebSocketSession> sendRequest : id_sessions.entrySet()) {
                if(sendRequest.getKey().equals(   friendsId.toString()))
                    sendRequest.getValue().sendMessage(new TextMessage("{\"Message\":\"You got notification\"}"));
            }
        }

    }

    public void notifyUser(Long userId) throws IOException {
        //System.out.println("User id to notify: " +userId);
        //System.out.println("Connections: " + id_sessions.size() );
        for (Map.Entry<String, WebSocketSession> sendRequest : id_sessions.entrySet()) {
            //System.out.println(sendRequest.getKey());
            if(userId!=null){
                if(sendRequest.getKey().equals(userId.toString()) ) {
                    //System.out.println("Found user, notifying now");
                    sendRequest.getValue().sendMessage(new TextMessage("{\"Message\":\"You got notification\"}"));
                    break;
                }
            }
        }
    }


    public String getIdFromConnections(WebSocketSession session){
        for (Map.Entry<String, WebSocketSession> users : id_sessions.entrySet()) {
            System.out.println(users.getValue().getId());
            if(users.getValue().getId() == session.getId()) {
                //userService.setOfflineStatus(Long.parseLong(users.getValue().getId()));
                //return users.getValue().getId();
                return users.getKey();
            }
        }
        return null;
    }



}