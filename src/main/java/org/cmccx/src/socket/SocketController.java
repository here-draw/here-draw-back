package org.cmccx.src.socket;

import org.cmccx.src.chat.model.ChatMessageReq;
import org.cmccx.src.chat.model.SocketChatRoom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SocketController {
    private final SocketService socketService;

    @Autowired
    public SocketController(SocketService socketService) {
        this.socketService = socketService;
    }

    @MessageMapping("/chat/enter")
    public void enterChatroom(SocketChatRoom socketChatRoom) {
        socketService.enterChatroom(socketChatRoom);
    }

    @MessageMapping("/chat/exit")
    public void exitChatroom(SocketChatRoom socketChatRoom) {
        socketService.exitChatroom(socketChatRoom);
    }

    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageReq message){
        socketService.sendMessage(message);
    }
}
