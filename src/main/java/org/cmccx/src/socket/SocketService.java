package org.cmccx.src.socket;

import org.cmccx.config.BaseException;
import org.cmccx.src.chat.ChatProvider;
import org.cmccx.src.chat.ChatService;
import org.cmccx.src.chat.model.ChatMessageReq;
import org.cmccx.utils.WordFiltering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.cmccx.config.BaseResponseStatus.*;

@Service
public class SocketService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static Map<Long, String> websocketConnectedUsers = Collections.synchronizedMap(new HashMap<>());  // 웹소켓 연결 사용자

    private final SimpMessagingTemplate messagingTemplate;

    private final WordFiltering wordFiltering;
    private final ChatProvider chatProvider;
    private final ChatService chatService;

    @Autowired
    public SocketService(SimpMessagingTemplate messagingTemplate, WordFiltering wordFiltering, ChatProvider chatProvider, ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.wordFiltering = wordFiltering;
        this.chatProvider = chatProvider;
        this.chatService = chatService;
    }

    /** 소켓 연결 시 회원 추가 **/
    public static void connectWebsocket(String sessionId, long userId) {
        websocketConnectedUsers.put(userId, sessionId);
    }

    /** 소켓 연결 해제 시 회원 제거 **/
    public static void disconnectWebsocket(String sessionId) {
        websocketConnectedUsers.entrySet().removeIf(entry -> entry.getValue().equals(sessionId));
    }

    /** 메세지 발송 **/
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void sendMessage(ChatMessageReq messageInfo, String sessionId) throws BaseException {
        long roomId = messageInfo.getRoomId();
        long senderId = messageInfo.getSenderId();
        long receiverId = messageInfo.getReceiverId();

        try {
            // 채팅방 소유 여부 확인
            int isValid = chatProvider.checkUserChatRoom(roomId, senderId);
            if (isValid == 0) {
                throw new BaseException(BAD_REQUEST);
            }

            // 상대 채팅방 확인
            int isValidReceiver = chatProvider.checkUserChatRoom(roomId, receiverId);
            if (isValidReceiver == 0) {
                throw new BaseException(BAD_REQUEST);
            }

            // 금지어 제한
            boolean isForbidden = wordFiltering.blankCheck(messageInfo.getMessage());
            if (isForbidden) {
                throw new BaseException(INCLUDED_FORBIDDEN_WORD);
            }

            // 상대방이 나간 경우, 채팅방 활성화
            int isContactUserValid = chatProvider.checkUserChatRoom(roomId, receiverId);
            if (isContactUserValid == 0) {
                chatService.modifyUserChatroomStatus(receiverId, roomId);
            }

            // 메세지 DB 저장
            chatService.registerMessage(messageInfo);

            // 메세지 전송
            messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, messageInfo);

            // 상대방이 온라인이 아닐 경우 , 개별 fcm
            if (!websocketConnectedUsers.containsKey(receiverId)) {

            }
        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("채팅 에러", e);
            throw new BaseException(DATABASE_ERROR);
        }
   }
}
