package org.cmccx.src.chat;

import org.cmccx.config.BaseException;
import org.cmccx.config.BaseResponseStatus;
import org.cmccx.src.chat.model.GetChatRoomsRes;
import org.cmccx.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.cmccx.config.BaseResponseStatus.DATABASE_ERROR;

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

    /**
     * 회원의 채팅방 소유 여부 확인
     **/
    public int checkUserChatRoom(long roomId, long userId) throws BaseException {
        try {
            return chatDao.checkChatRoomId(roomId, userId);
        } catch (Exception e) {
            logger.error("CheckUserChatRoom Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 채팅 목록 조회
     **/
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
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
