package org.cmccx.src.socket;

import org.cmccx.config.BaseException;
import org.cmccx.src.chat.model.ChatMessageReq;
import org.cmccx.src.chat.model.ChatMessageErrorRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.cmccx.config.BaseResponseStatus.VALIDATION_ERROR;

@RestController
public class SocketController {
    private final SocketService socketService;

    @Autowired
    public SocketController(SocketService socketService) {
        this.socketService = socketService;
    }

    @MessageMapping("/chat/message")
    public void sendMessage(@Valid ChatMessageReq message, SimpMessageHeaderAccessor accessor) throws BaseException {
        socketService.sendMessage(message, accessor.getSessionId());
    }

    @MessageExceptionHandler(BaseException.class)
    @SendToUser("/queue/errors")
    public ChatMessageErrorRes handelException(BaseException e) {
        ChatMessageErrorRes error = new ChatMessageErrorRes(e.getStatus().getCode(), e.getStatus().getMessage());

        return error;
    }

    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    @SendToUser("/queue/errors")
    public ChatMessageErrorRes handelValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        ChatMessageErrorRes error = new ChatMessageErrorRes(VALIDATION_ERROR.getCode(), errorMessage);

        return error;
    }
}
