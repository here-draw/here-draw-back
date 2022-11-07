package org.cmccx.src.chat;

import org.cmccx.config.BaseException;
import org.cmccx.config.BaseResponse;
import org.cmccx.src.chat.model.GetChatRoomInfoRes;
import org.cmccx.src.chat.model.GetChatRoomMessagesRes;
import org.cmccx.src.chat.model.GetChatRoomsRes;
import org.cmccx.src.chat.model.PostChatRoomForArtReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/chats")
@Validated
public class ChatController {
    private final ChatService chatService;
    private final ChatProvider chatProvider;

    @Autowired
    public ChatController(ChatService chatService, ChatProvider chatProvider) {
        this.chatService = chatService;
        this.chatProvider = chatProvider;
    }

    /**
     * 작품 문의 및 구매 채팅방 생성 API
     * [POST] /chats/arts/{art-id}
     * @return BaseResponse<GetChatRoomInfoRes>
     */
    @ResponseBody
    @PostMapping("/arts/{art-id}")
    public BaseResponse<GetChatRoomInfoRes> createChatRoom(@PathVariable("art-id") Long artId,
                                                           @RequestBody @Valid PostChatRoomForArtReq postChatRoomForArtReq) throws BaseException {
        postChatRoomForArtReq.setArtId(artId);
        GetChatRoomInfoRes result = chatService.createChatRoomForArt(postChatRoomForArtReq);

        return new BaseResponse<>(result);
    }

    /**
     * 작가에게 DM 채팅방 생성 API
     * [POST] /chats/artists/{artist-id}
     * @return BaseResponse<GetChatRoomInfoRes>
     */
    @ResponseBody
    @PostMapping("/artists/{artist-id}")
    public BaseResponse<GetChatRoomInfoRes> createChatRoom(@PathVariable("artist-id") long artistId) throws BaseException {
        GetChatRoomInfoRes result = chatService.createChatRoomForArtist(new PostChatRoomForArtReq(artistId, null, null, null));

        return new BaseResponse<>(result);
    }

    /**
     * 채팅방 목록 조회 API
     * [GET] /chats/rooms
     * @return BaseResponse<List<GetChatRoomsRes>>
     */
    @ResponseBody
    @GetMapping("/rooms")
    public BaseResponse<List<GetChatRoomsRes>> getChatRooms() throws BaseException {
        List<GetChatRoomsRes> result = chatProvider.getChatRooms();

        return new BaseResponse<>(result);
    }

    /**
     * 채팅방 정보 조회 API
     * [GET] /chats/rooms/{room-id}
     * @return BaseResponse<GetChatRoomInfoRes>
     */
    @ResponseBody
    @GetMapping("/rooms/{room-id}")
    public BaseResponse<GetChatRoomInfoRes> getChatRoomInfo(@PathVariable("room-id") long roomId) throws BaseException {
        GetChatRoomInfoRes result = chatProvider.getChatRoomInfo(roomId);

        return new BaseResponse<>(result);
    }

    /**
     * 채팅방 메세지 조회 API
     * [GET] /chats/rooms/{room-id}/messages?id={id}&date={date}
     * @return BaseResponse<GetChatRoomMessagesRes>
     */
    @ResponseBody
    @GetMapping("rooms/{room-id}/messages")
    public BaseResponse<GetChatRoomMessagesRes> getChatroomMessages(@PathVariable("room-id") long roomId,
                                                                    @RequestParam(value = "id", defaultValue = "0") long messageId,
                                                                    @RequestParam(value = "date") @NotBlank(message = "마지막 메세지 날짜를 입력하세요.") String date) throws BaseException {
        GetChatRoomMessagesRes result = chatProvider.getChatRoomMessage(roomId, messageId, date);

        return new BaseResponse<>(result);
    }

    /**
     * 채팅방 나가기 API
     * [DELETE] /chats/rooms/{room-id}
     * @return BaseResponse<List<GetChatRoomsRes>>
     */
    @ResponseBody
    @DeleteMapping("/rooms/{room-id}")
    public BaseResponse<String> exitChatRoom(@PathVariable("room-id") long roomId) throws BaseException {
        String result = chatService.exitChatRoom(roomId);

        return new BaseResponse<>(result);
    }
}
