package org.cmccx.src.chat;

import org.cmccx.config.BaseException;
import org.cmccx.config.Constant.RoomType;
import org.cmccx.src.art.ArtProvider;
import org.cmccx.src.chat.model.ChatMessageReq;
import org.cmccx.src.chat.model.GetChatRoomInfoRes;
import org.cmccx.src.chat.model.GetExistentChatRoomData;
import org.cmccx.src.chat.model.PostChatRoomForArtReq;
import org.cmccx.src.trade.TradeProvider;
import org.cmccx.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.cmccx.config.BaseResponseStatus.*;

@Service
public class ChatService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatProvider chatProvider;
    private final ChatDao chatDao;
    private final ArtProvider artProvider;
    private final TradeProvider tradeProvider;
    private final JwtService jwtService;

    @Autowired
    public ChatService(SimpMessagingTemplate messagingTemplate, ChatProvider chatProvider, ChatDao chatDao, ArtProvider artProvider, TradeProvider tradeProvider, JwtService jwtService) {
        this.messagingTemplate = messagingTemplate;
        this.chatProvider = chatProvider;
        this.chatDao = chatDao;
        this.artProvider = artProvider;
        this.tradeProvider = tradeProvider;
        this.jwtService = jwtService;
    }

    /** 메세지 저장 **/
    public void registerMessage(ChatMessageReq messageInfo) throws BaseException {
        try {
            chatDao.insertMessage(messageInfo);
        } catch (Exception e) {
            logger.error("RegisterMessage Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /** 작품 문의 및 구매 채팅방 생성 **/
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public GetChatRoomInfoRes createChatRoomForArt(PostChatRoomForArtReq postChatRoomForArtReq) throws BaseException {
        try {
             //회원 검증 및 ID 추출
             long userId = jwtService.getUserId();

            // 채팅 상대 ID 유효성 검사
//            int isValidUser = userDao.checkUserId(postChatRoomForArtReq.getArtistId());
//            if (isValidUser == 0) {
//                throw new BaseException(FAILED_ACCESS_USER);    // 탈퇴 또는 차단된 사용자입니다. 아니면 개별 에러?
//            }

            // 작품 ID 유효성 검사
            int isValidArt = artProvider.checkArt(postChatRoomForArtReq.getArtId());
            if (isValidArt == 0) {
                throw new BaseException(FAILED_ACCESS_ART);
            }

            // 작품 채팅방 존재 여부 확인
            GetExistentChatRoomData chatRoom = chatProvider.checkExistentChatRoom(RoomType.INQUIRY, postChatRoomForArtReq.getArtId(), userId, postChatRoomForArtReq.getArtistId());
            // 채팅방이 있는 경우
            if (chatRoom.isExistent()) {
                long roomId = chatRoom.getRoomId();
                // 채팅방 정보 업데이트(문의에서 구매로 넘어가는 경우)
                if (!chatRoom.isPurchase()) {
                    chatDao.updateChatRoom(roomId, postChatRoomForArtReq);
                }

                // 채팅방을 나간 회원 재연결
                if (chatRoom.getUserStatus().equals("I")) {
                    chatDao.updateUserChatRoom(userId, roomId);
                } else if (chatRoom.getContactUserStatus().equals("I")) {
                    chatDao.updateUserChatRoom(postChatRoomForArtReq.getArtistId(), roomId);
                }

                return chatProvider.getChatRoomInfo(roomId);
            }
            // 채팅방 생성
            long roomId = chatDao.insertChatRoom(postChatRoomForArtReq);
            // 회원마다 채팅방 연결
            chatDao.insertUserChatRoom(new long[]{userId, postChatRoomForArtReq.getArtistId()} ,roomId);

            return chatProvider.getChatRoomInfo(roomId);

        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("CreateChatRoomForArt Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /** 작가에게 DM 채팅방 생성 **/
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public GetChatRoomInfoRes createChatRoomForArtist(PostChatRoomForArtReq postChatRoomForArtReq) throws BaseException {
        try {
            //회원 검증 및 ID 추출
            long userId = jwtService.getUserId();

//            // 채팅 상대 ID 유효성 검사
//            if (isValidUser == 0) {
//                throw new BaseException(FAILED_ACCESS_USER);    // 탈퇴 또는 차단된 사용자입니다. 아니면 개별 에러?
//            }

            // DM 채팅방 존재 여부 확인
            GetExistentChatRoomData chatRoom = chatProvider.checkExistentChatRoom(RoomType.DIRECT_MESSAGE, 0, userId, postChatRoomForArtReq.getArtistId());
            // 채팅방이 있는 경우
            if (chatRoom.isExistent()) {
                long roomId = chatRoom.getRoomId();

                // 채팅방을 나간 회원 재연결
                if (chatRoom.getUserStatus().equals("I")) {
                    chatDao.updateUserChatRoom(userId, roomId);
                } else if (chatRoom.getContactUserStatus().equals("I")) {
                    chatDao.updateUserChatRoom(postChatRoomForArtReq.getArtistId(), roomId);
                }

                return chatProvider.getChatRoomInfo(roomId);
            }
            // 채팅방 생성
            long roomId = chatDao.insertChatRoom(postChatRoomForArtReq);
            // 회원마다 채팅방 연결
            chatDao.insertUserChatRoom(new long[]{userId, postChatRoomForArtReq.getArtistId()} ,roomId);

            return chatProvider.getChatRoomInfo(roomId);

        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("CreateChatRoomForArtist Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /** 유저 메세지 방 연결 **/
    public void modifyUserChatroomStatus(long userId, long roomId) throws BaseException {
        try {
            chatDao.updateUserChatRoom(userId, roomId);
        } catch (Exception e) {
            logger.error("ModifyUserChatroomStatus Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /** 채팅방 나가기 **/
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public String exitChatRoom(long roomId) throws BaseException {
        try {
            //회원 검증 및 ID 추출
            long userId = jwtService.getUserId();

            // 채팅방 소유 여부 확인
            int isValid = chatProvider.checkUserChatRoom(roomId, userId);
            if (isValid == 0) {
                throw new BaseException(BAD_REQUEST);
            }

            // 거래 내역이 있는지 확인
            String isExit = tradeProvider.getTradeStatus(roomId);
            if (isExit != null) {
                if (!isExit.equals('A')) {
                    throw new BaseException(FAIL_EXIT_CHATROOM);
                }
            }

            // 채팅방과 회원 매핑 관계 해제
            boolean isDisabled = chatDao.deleteUserChatRoom(userId, roomId);
            if (isDisabled) { // 양쪽 회원이 모두 채팅방에 없을 경우, 채팅방 완전 삭제
                int row = chatDao.deleteChatRoom(roomId);
                if (row == 0){
                    throw new BaseException(BAD_REQUEST);
                }
            }

            String result = "채팅방에서 나갔습니다.";
            return result;

        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("CreateChatRoomForArtist Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
