package org.cmccx.src.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/chats")
public class ChatController {
    private final ChatService chatService;
    private final ChatProvider chatProvider;

    @Autowired
    public ChatController(ChatService chatService, ChatProvider chatProvider) {
        this.chatService = chatService;
        this.chatProvider = chatProvider;
    }

    /**
     * 채팅방 목록 조회 API
     * [GET] /chat/{user-id}/rooms/
     * @return BaseResponse<>
     */
    @ResponseBody
    @GetMapping("/{user-id}/rooms")
    public void getChatRooms(@PathVariable("user-id") long userId) {

    }

}
