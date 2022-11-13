package org.cmccx.src.chat;

import org.cmccx.config.BaseException;
import org.cmccx.config.Constant.RoomType;
import org.cmccx.src.chat.model.*;
import org.cmccx.utils.JwtService;
import org.cmccx.utils.ScrollPagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.cmccx.config.BaseResponseStatus.*;

@Service
public class ChatProvider {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ChatDao chatDao;
    private final JwtService jwtService;

    @Autowired
    public ChatProvider(ChatDao chatDao, JwtService jwtService) {
        this.chatDao = chatDao;
        this.jwtService = jwtService;
    }

    /** 이미 생성된 채팅방인지 확인 **/
    public GetExistentChatRoomData checkExistentChatRoom(RoomType type, long artId, long userId, long contactUserId) throws BaseException {
        try {
            return chatDao.checkChatRoomByArt(type, artId, userId, contactUserId);
        } catch (Exception e) {
            logger.error("CheckExistentChatRoom Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /** 회원의 채팅방 소유 여부 확인 **/
    public int checkUserChatRoom(long roomId, long userId) throws BaseException {
        try {
            return chatDao.checkChatRoomId(roomId, userId);
        } catch (Exception e) {
            logger.error("CheckUserChatRoom Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /** 채팅 목록 조회 **/
    public List<GetChatRoomsRes> getChatRooms() throws BaseException {
        try {
            // 회원 검증 및 ID 추출
            long userId = jwtService.getUserId();

            // 채팅방 목록 조회
            List<GetChatRoomsRes> result = chatDao.selectChatRooms(userId);
            return result;

        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("GetChatRooms Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /** 채팅방 정보 조회 **/
    public GetChatRoomInfoRes getChatRoomInfo(long roomId) throws BaseException {
        try {
            // 회원 검증 및 ID 추출
            long userId = jwtService.getUserId();

            // 채팅방 소유 여부 확인
            int isValid = chatDao.checkChatRoomId(roomId, userId);
            if (isValid == 0) {
                throw new BaseException(BAD_REQUEST);
            }

            // 채팅방 정보 조회
            GetChatRoomInfoRes result = chatDao.selectChatRoomInfo(userId, roomId);
            return result;

        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("GetChatRoomData Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /** 채팅방 메세지 조회 **/
    public GetChatRoomMessagesRes getChatRoomMessage(long roomId, long messageId, String date) throws BaseException {
        try {
            // 회원 검증 및 ID 추출
            long userId = jwtService.getUserId();

            // 채팅방 소유 확인
            int isValid = chatDao.checkChatRoomId(roomId, userId);
            if (isValid == 0) {
                throw new BaseException(BAD_REQUEST);
            }

            // 메세지 목록 조회(오래된 순으로 20개)
            List<ChatRoomMessage> messageList = chatDao.selectChatRoomMessages(userId, roomId, messageId, date);
            ScrollPagination<ChatRoomMessage> scrollInfo = ScrollPagination.of(messageList, 20);
            GetChatRoomMessagesRes result = GetChatRoomMessagesRes.of(scrollInfo);

            return result;

        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("GetChatRoomMessage Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }
}
