package org.cmccx.src.socket;

import org.cmccx.config.BaseException;
import org.cmccx.src.chat.ChatProvider;
import org.cmccx.src.chat.ChatService;
import org.cmccx.src.chat.model.ChatMessageReq;
import org.cmccx.src.chat.model.SocketChatRoom;
import org.cmccx.utils.WordFiltering;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static org.cmccx.config.BaseResponseStatus.BAD_REQUEST;
import static org.cmccx.config.BaseResponseStatus.INCLUDED_FORBIDDEN_WORD;

@Service
public class SocketService {
    public static List<Long> websocketConnectedUsers = Collections.synchronizedList(new ArrayList<>());  // 웹소켓 연결 사용자
    public static Map<Long, List<Long>> chatRoomLoggedUsers = Collections.synchronizedMap(new HashMap<>());   // 채팅방을 보고 있는 사용자

    private final SimpMessagingTemplate messagingTemplate;
    private final SimpMessageSendingOperations messageSendingOperations;

    private final WordFiltering wordFiltering;
    private final ChatProvider chatProvider;
    private final ChatService chatService;

    @Autowired
    public SocketService(SimpMessagingTemplate messagingTemplate, SimpMessageSendingOperations messageSendingOperations, WordFiltering wordFiltering, ChatProvider chatProvider, ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.messageSendingOperations = messageSendingOperations;
        this.wordFiltering = wordFiltering;
        this.chatProvider = chatProvider;
        this.chatService = chatService;
    }

    /** 소켓 연결 시 회원 추가 **/
    public List<Long> connectWebsocket(long userId) {
        websocketConnectedUsers.add(userId);
        websocketConnectedUsers = websocketConnectedUsers.parallelStream()
                                                            .distinct()
                                                            .collect(Collectors.toList());
        System.out.println(websocketConnectedUsers);
        return websocketConnectedUsers;
    }

    /** 소켓 연결 해제 시 회원 제거 **/
    public List<Long> disconnectWebsocket(long userId) {
        websocketConnectedUsers.remove(userId);
        websocketConnectedUsers = websocketConnectedUsers.parallelStream()
                .distinct()
                .collect(Collectors.toList());
        System.out.println(websocketConnectedUsers);
        return websocketConnectedUsers;
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
    public void enterChatroom(SocketChatRoom socketChatRoom) {
        // 채팅방에 접속으로 처리
        List<Long> users = addUser(socketChatRoom.getRoomId(), socketChatRoom.getUserId());
        // 채팅방 구독
        messagingTemplate.convertAndSend("/sub/chat/room" + socketChatRoom.getRoomId(), users);
    }

    /** 채팅방 퇴장 **/
    public void exitChatroom(SocketChatRoom socketChatRoom) {
        // 채팅방에서 접속 해제
        List<Long> users = removeUser(socketChatRoom.getRoomId(), socketChatRoom.getUserId());
        // 채팅방 구독 해제
        messagingTemplate.convertAndSend("/sub/chat/room" + socketChatRoom.getRoomId(), users);
    }

    /** 메세지 발송**/
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void sendMessage(ChatMessageReq messageInfo) {
        long roomId = messageInfo.getRoomId();
        long senderId = messageInfo.getSenderId();
        long receiverId = messageInfo.getReceiverId();

        try {
            // 금지어 제한
            boolean isForbidden = wordFiltering.blankCheck(messageInfo.getMessage());
            if (isForbidden) {
                System.out.println("BAD WORD");
                throw new BaseException(INCLUDED_FORBIDDEN_WORD);
            }

            // 채팅방 소유 여부 확인
            int isValid = chatProvider.checkUserChatRoom(roomId, senderId);
            if (isValid == 0) {
                throw new BaseException(BAD_REQUEST);
            }

            // 메세지 DB 저장
            chatService.registerMessage(messageInfo);

            // 메세지 전송
            messagingTemplate.convertAndSend("sub/chat/message" + roomId, messageInfo);

            // 상대방이 나간 경우, 채팅방 활성화
            int isContactUserValid = chatProvider.checkUserChatRoom(roomId, receiverId);
            if (isContactUserValid == 0) {
                chatService.modifyUserChatroomStatus(receiverId, roomId);
            }

            // 온라인이 아닐 경우 푸시

        } catch (BaseException e) {

        }
   }
}
