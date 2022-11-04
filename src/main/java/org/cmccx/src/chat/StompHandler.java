//package org.cmccx.src.chat;
//
//import org.cmccx.config.BaseException;
//import org.cmccx.utils.JwtService;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.simp.stomp.StompCommand;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.messaging.support.ChannelInterceptor;
//
//public class StompHandler implements ChannelInterceptor {
//    @Override
//    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//        JwtService jwtService = new JwtService();
//
//        if (StompCommand.CONNECT == accessor.getCommand()) {
//            String token = accessor.getFirstNativeHeader("X-ACCESS-TOKEN");
//            try {
//                jwtService.getUserId()
//            } catch (BaseException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return ChannelInterceptor.super.preSend(message, channel);
//    }
//}
