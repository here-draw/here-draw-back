package org.cmccx.src.socket;

import org.cmccx.config.BaseException;
import org.cmccx.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class StompHandler implements ChannelInterceptor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JwtService jwtService;

    @Autowired
    public StompHandler(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) { // 메세지 발신 전 사전 작업
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String jwt = accessor.getNativeHeader("X-ACCESS-TOKEN").get(0);
            try {
                // JWT 검증 및 세션 등록
                long userId = jwtService.getUserId(jwt);
                SocketService.connectWebsocket(accessor.getSessionId(), userId);
                System.out.println("핸들러 Connect");
            } catch (BaseException e) {
                // 발생한 BaseException을 원인 예외로 등록하여 MessagingException throw
                MessagingException messagingException = new MessagingException("JWT");
                messagingException.initCause(e);
                System.out.println("핸들러 JWT 인증 실패");
                throw messagingException;
            }
        } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            // 세션 해제
            SocketService.disconnectWebsocket(accessor.getSessionId());
            System.out.println("핸들러 Disconnect");
        }

        return message;
    }
}