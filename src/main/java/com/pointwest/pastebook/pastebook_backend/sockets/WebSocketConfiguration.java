package com.pointwest.pastebook.pastebook_backend.sockets;

import com.pointwest.pastebook.pastebook_backend.services.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;


import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

    @Autowired
    private SocketHandler socketHandler;



    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        //registry.addHandler(new SocketHandler(), "/onlineconnection")
        registry.addHandler(socketHandler, "/onlineconnection")
                .setAllowedOrigins("*")
                // initial Request/Handshake interceptor
                .addInterceptors(new HttpSessionHandshakeInterceptor() {

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, @Nullable Exception ex) {
                        super.afterHandshake(request, response, wsHandler, ex);

                    }

                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request,
                                                   ServerHttpResponse response, WebSocketHandler wsHandler,
                                                   Map<String, Object> attributes) throws Exception {
                        boolean b = super.beforeHandshake(request, response, wsHandler, attributes);
                        // && (request.getPrincipal()).isAuthenticated();
                        return b;
                    }

                });
        // for IE8, 9 support
        // https://docs.spring.io/spring-framework/docs/5.0.0.M1/spring-framework-reference/html/websocket.html#websocket-fallback-xhr-vs-iframe
        // .withSockJS();

    }


    // @Bean
    // public ServletServerContainerFactoryBean createWebSocketContainer() {
    //     ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
    //     container.setMaxTextMessageBufferSize(8192);
    //     container.setMaxBinaryMessageBufferSize(8192);
    //     return container;
    // }

}