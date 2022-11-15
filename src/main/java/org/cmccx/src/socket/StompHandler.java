package org.cmccx.src.socket;

import org.cmccx.utils.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class StompHandler implements ChannelInterceptor {
    private final JwtService jwtService;

    @Autowired
    public StompHandler(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) { // 메세지 발신 전 사전 작업
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (accessor.getCommand() == StompCommand.CONNECT) {


        } else if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
            // String jwt = accessor.getNativeHeader("X-ACCESS-TOKEN").get(0);
            // JWT 인증
        } else if (accessor.getCommand() == StompCommand.DISCONNECT) {
            //long userId = (long) accessor.getSessionAttributes().get("userId");
            //System.out.println("갑자기 꺼짐");
        }
        return message;
    }
}