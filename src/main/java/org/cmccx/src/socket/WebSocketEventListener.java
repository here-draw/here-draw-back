package org.cmccx.src.socket;

import org.cmccx.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Component
public class WebSocketEventListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JwtService jwtService;
    private final SocketService socketService;

    @Autowired
    public WebSocketEventListener(JwtService jwtService, SocketService socketService) {
        this.jwtService = jwtService;
        this.socketService = socketService;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String jwt = (String) accessor.getFirstNativeHeader("X-ACCESS-TOKEN");

        // JWT 인증
        long userId = jwtService.getUserId(jwt);

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        sessionAttributes.put("userId", userId);
        accessor.setSessionAttributes(sessionAttributes);

        socketService.connectWebsocket(userId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        long userId = (long) accessor.getSessionAttributes().get("userId");

        socketService.disconnectWebsocket(userId);
    }
}
