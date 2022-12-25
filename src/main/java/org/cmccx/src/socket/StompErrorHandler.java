package org.cmccx.src.socket;

import org.cmccx.config.BaseException;
import org.cmccx.config.BaseResponseStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;

@Component
public class StompErrorHandler extends StompSubProtocolErrorHandler {

    public StompErrorHandler() {
        super();
    }

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        if (ex.getCause() instanceof BaseException) {
            return handleBaseException((BaseException)ex.getCause());
        } else if (ex instanceof BaseException) {
            return handleBaseException((BaseException)ex);
        } else {
            return super.handleClientMessageProcessingError(clientMessage, ex);
        }
    }

    /* 예외 처리 */
    private Message<byte[]> handleBaseException(BaseException e) {
        return createErrorMessage(e.getStatus());
    }

    /* 에러 메세지 생성 */
    private Message<byte[]> createErrorMessage(BaseResponseStatus e) {
        String code = String.valueOf(e.getCode());
        String message = e.getMessage();

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);

        accessor.setMessage(code);
        accessor.setLeaveMutable(true);

        return MessageBuilder.createMessage(message.getBytes(StandardCharsets.UTF_8), accessor.getMessageHeaders());
    }
}
