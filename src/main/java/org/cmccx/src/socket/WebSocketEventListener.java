package org.cmccx.src.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final SocketService socketService;

    @Autowired
    public WebSocketEventListener(SocketService socketService) {
        this.socketService = socketService;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
//        MessageHeaderAccessor accessor = NativeMessageHeaderAccessor.getAccessor(event.getMessage(), SimpMessageHeaderAccessor.class);
//        GenericMessage generic = (GenericMessage) accessor.getHeader("SimpConnectMessage");
//        Map nativeHeaders = (Map) generic.getHeaders().get("nativeHeaders");
//        String jwt = (String) nativeHeaders.get("X-ACCESS-TOKEN");

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        accessor.getMessageHeaders();

        //GenericMessage msg = (GenericMessage) accessor.getMessageHeaders().get("X-ACCESS-TOKEN");
        System.out.println(sessionId);
        System.out.println("리스너 동작");

        //socketService.connectWebsocket(userId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        System.out.println("연결 끊김");
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        //long userId = (long) accessor.getSessionAttributes().get("userId");
        //socketService.disconnectWebsocket(userId);
    }
}
