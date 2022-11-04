package org.cmccx.src.chat;

import org.cmccx.config.BaseException;
import org.cmccx.src.art.ArtProvider;
import org.cmccx.src.chat.model.GetChatRoomsRes;
import org.cmccx.src.chat.model.PostChatRoomForArtReq;
import org.cmccx.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.cmccx.config.BaseResponseStatus.DATABASE_ERROR;
import static org.cmccx.config.BaseResponseStatus.FAILED_ACCESS_ART;

@Service
public class ChatService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatDao chatDao;
    private final ArtProvider artProvider;
    private final JwtService jwtService;

    @Autowired
    public ChatService(SimpMessagingTemplate messagingTemplate, ChatDao chatDao, ArtProvider artProvider, JwtService jwtService) {
        this.messagingTemplate = messagingTemplate;
        this.chatDao = chatDao;
        this.artProvider = artProvider;
        this.jwtService = jwtService;
    }

    /** 작품 문의 또는 구매 채팅방 생성 **/
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void createChatRoomForArt(PostChatRoomForArtReq postChatRoomForArtReq) throws BaseException {
        try {
             //회원 검증 및 ID 추출
             long userId = jwtService.getUserId();

//            // 채팅 상대 ID 유효성 검사
//            if (isValidUser == 0) {
//                throw new BaseException();    // 탈퇴 또는 차단된 사용자입니다. 아니면 개별 에러?
//            }

            // 작품 ID 유효성 검사
            int isValidArt = artProvider.checkArt(postChatRoomForArtReq.getArtId());
            if (isValidArt == 0) {
                throw new BaseException(FAILED_ACCESS_ART);
            }

            // 채팅방 존재 여부 확인
            // 채팅방이 있고, 유저에 따라 나갔던 채팅방인 경우 다시 살리거나 새로 연결시킴

            // 채팅방 생성
            int roomId = chatDao.insertChatRoom(postChatRoomForArtReq);
            // 회원마다 채팅방 연결
            chatDao.insertUserChatRoom(new long[]{userId, postChatRoomForArtReq.getArtistId()} ,roomId);
        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("CreateChatRoomForArt Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /** DM 채팅방 생성 **/
    public void createChatRoomForArtist(long userId) {

    }




}
