package org.cmccx.src.socket;

import org.cmccx.config.BaseException;
import org.cmccx.src.chat.ChatProvider;
import org.cmccx.src.chat.ChatService;
import org.cmccx.src.chat.model.ChatMessageReq;
import org.cmccx.utils.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static org.cmccx.config.BaseResponseStatus.BAD_REQUEST;

@Service
public class SocketService {
    public static List<Long> websocketConnectedUsers = Collections.synchronizedList(new ArrayList<>());  // 웹소켓 연결 사용자
    public static Map<Long, List<Long>> chatRoomLoggedUsers = Collections.synchronizedMap(new HashMap<>());   // 채팅방을 보고 있는 사용자

    private final SimpMessagingTemplate messagingTemplate;
    private final SimpMessageSendingOperations messageSendingOperations;

    private final JwtService jwtService;
    private final ChatProvider chatProvider;
    private final ChatService chatService;

    @Autowired
    public SocketService(SimpMessagingTemplate messagingTemplate, SimpMessageSendingOperations messageSendingOperations, JwtService jwtService, ChatProvider chatProvider, ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.messageSendingOperations = messageSendingOperations;
        this.jwtService = jwtService;
        this.chatProvider = chatProvider;
        this.chatService = chatService;
    }

    /** 채팅방 접속 회원 추가 **/
    private List<Long> addUser(long roomId, long userId) {
        if (chatRoomLoggedUsers.isEmpty() || !chatRoomLoggedUsers.containsKey(roomId)) {
            chatRoomLoggedUsers.put(roomId, new ArrayList<>());
        }

        List<Long> users = chatRoomLoggedUsers.get(roomId);

        if (!users.contains(userId)) {
            users.add(userId);
        }

        return users.stream().distinct().collect(Collectors.toList());
    }

    /** 채팅방 접속 회원 해제 **/
    private List<Long> removeUser(long roomId, long userId) {
        if (chatRoomLoggedUsers.isEmpty() || !chatRoomLoggedUsers.containsKey(roomId)) {
            return new ArrayList<>();
        }

        List<Long> users = chatRoomLoggedUsers.get(roomId);

        if (users.size() == 0) {
            chatRoomLoggedUsers.remove(roomId);
            return new ArrayList<>();
        }

        if (!users.contains(userId)) {
            return users;
        }

        users.remove(userId);

        if (users.size() == 0) {
            chatRoomLoggedUsers.remove(roomId);
        }

        return users.stream().distinct().collect(Collectors.toList());
    }

    /** 채팅방 입장 **/
    public void enterChatroom(long roomId, long userId) {
        // 채팅방에 접속으로 처리
        List<Long> users = addUser(roomId, userId);
        // 채팅방 구독
        messagingTemplate.convertAndSend("/sub/chat/room" + roomId, users);
    }

    /** 채팅방 퇴장 **/
    public void exitChatroom(long roomId, long userId) {
        // 채팅방에서 접속 해제
        List<Long> users = removeUser(roomId, userId);
        // 채팅방 구독 해제
        messagingTemplate.convertAndSend("/sub/chat/room" + roomId, users);
    }

    /** 메세지 발송**/
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void sendMessage(ChatMessageReq messageInfo) throws BaseException {
        long roomId = messageInfo.getRoomId();

        try {
            //회원 검증 및 ID 추출
            long userId = jwtService.getUserId();

            // 금지어 제한한


            // 채팅방 소유 여부 확인
            int isValid = chatProvider.checkUserChatRoom(roomId, userId);
            if (isValid == 0) {
                throw new BaseException(BAD_REQUEST);
            }

            // 메세지 전송
            chatService.registerMessage(messageInfo);


        } catch (BaseException e) {

        }
   }
}
