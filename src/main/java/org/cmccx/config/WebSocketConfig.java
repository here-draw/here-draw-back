package org.cmccx.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket Handshake Connection을 생성할 경로 지정
        registry.addEndpoint("/wss-stomp").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Client로 메세지 송신할 때 prefix
        // SimpleBroker는 구독 중인 Client에게 메세지를 전달
        registry.enableSimpleBroker("/sub");
        // Client에서 메세지 수신할 때 prefix
        registry.setApplicationDestinationPrefixes("/pub");
        //서버에서 클라이언트로부터 메세지 받을 prefix
        registry.setUserDestinationPrefix("/user");
    }
}
