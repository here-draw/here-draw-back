//package org.cmccx.src.socket;
//
//import org.cmccx.config.BaseException;
//import org.cmccx.utils.JwtService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.simp.stomp.StompCommand;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.messaging.support.ChannelInterceptor;
//import org.springframework.stereotype.Component;
//
//@Component
//public class StompHandler implements ChannelInterceptor {
//    private final JwtService jwtService;
//
//    @Autowired
//    public StompHandler(JwtService jwtService) {
//        this.jwtService = jwtService;
//    }
//
//    @Override
//    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//        if (accessor.getCommand() == StompCommand.CONNECT) {
//            try {
//                jwtService.getUserId(accessor.getFirstNativeHeader("X-ACCESS-TOKEN"));
//            } catch (BaseException e) {
//                throw new BaseException(e.getStatus());
//            }
//        }
//
//        return message;
//    }
//}
