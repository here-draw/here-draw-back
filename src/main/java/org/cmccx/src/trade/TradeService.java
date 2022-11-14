package org.cmccx.src.trade;

import org.cmccx.config.BaseException;
import org.cmccx.src.art.ArtService;
import org.cmccx.src.chat.ChatProvider;
import org.cmccx.src.trade.model.PostTradeConfirmReq;
import org.cmccx.src.user.UserProvider;
import org.cmccx.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.cmccx.config.BaseResponseStatus.BAD_REQUEST;
import static org.cmccx.config.BaseResponseStatus.DATABASE_ERROR;

@Service
public class TradeService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TradeDao tradeDao;
    private final UserProvider userProvider;
    private final ChatProvider chatProvider;
    private final ArtService artService;
    private final JwtService jwtService;

    @Autowired
    public TradeService(TradeDao tradeDao, UserProvider userProvider, ChatProvider chatProvider, ArtService artService, JwtService jwtService) {
        this.tradeDao = tradeDao;
        this.userProvider = userProvider;
        this.chatProvider = chatProvider;
        this.artService = artService;
        this.jwtService = jwtService;
    }

    /** 거래 확정 **/
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public String registerTradeConfirm(PostTradeConfirmReq postTradeConfirmReq) throws BaseException {
        try {
            //회원 검증 및 ID 추출
            long userId = jwtService.getUserId();
            long artId = postTradeConfirmReq.getArtId();

            // 유효한 회원인지 확인
            int isValid = userProvider.checkUserId(userId);
            if(isValid == 0) {
                throw new BaseException(BAD_REQUEST);
            }

            // 채팅방 소유 여부 확인
            int isValidChatroom = chatProvider.checkUserChatRoom(postTradeConfirmReq.getRoomId(), userId);
            if (isValidChatroom == 0) {
                throw new BaseException(BAD_REQUEST);
            }

            // 거래 확정
            String status = tradeDao.insertTradeConfirm(postTradeConfirmReq, userId);
            if (status.equals("A")) {   // 거래 확정 완료된 경우
                // 독점 구매인 경우, 판매 완료 처리
                if (postTradeConfirmReq.getExclusive()) {
                    System.out.println("독점구매");
                    artService.updateArtStatus(artId, "E");
                } else {
                    // 판매 수량 증가
                    boolean isDone = artService.modifySalesQuantity(artId);
                    if (isDone) {   // 판매 수량이 소진된 경우
                        artService.updateArtStatus(artId, "F");
                    }
                }
            }

            String result = "거래가 확정되었습니다.";
            return result;

        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("RegisterTradeConfirm", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
