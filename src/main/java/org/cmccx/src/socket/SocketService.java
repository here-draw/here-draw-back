package org.cmccx.src.socket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SocketService {
    public static List<Long> websocketConnectedUsers = Collections.synchronizedList(new ArrayList<>());  // 웹소켓 연결 사용자
    public static Map<Long, List<Long>> chatRoomLoggedUsers = Collections.synchronizedMap(new HashMap<>());   // 채팅방을 보고 있는 사용자

    private final SimpMessagingTemplate messagingTemplate;
    private final SimpMessageSendingOperations messageSendingOperations;

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

    public void send() {
        // 금지어 제한한
   }
}
