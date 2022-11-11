package org.cmccx.src.trade;

import org.cmccx.config.BaseException;
import org.cmccx.src.chat.ChatProvider;
import org.cmccx.src.trade.model.PostTradeConfirmReq;
import org.cmccx.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.cmccx.config.BaseResponseStatus.BAD_REQUEST;
import static org.cmccx.config.BaseResponseStatus.DATABASE_ERROR;

@Service
public class TradeService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TradeDao tradeDao;
    private final ChatProvider chatProvider;
    private final JwtService jwtService;

    @Autowired
    public TradeService(TradeDao tradeDao, ChatProvider chatProvider, JwtService jwtService) {
        this.tradeDao = tradeDao;
        this.chatProvider = chatProvider;
        this.jwtService = jwtService;
    }

    /** 거래 확정 **/
    public String registerTradeConfirm(PostTradeConfirmReq postTradeConfirmReq) throws BaseException {
        try {
            //회원 검증 및 ID 추출
            long userId = jwtService.getUserId();

            // 채팅방 소유 여부 확인
            int isValid = chatProvider.checkUserChatRoom(postTradeConfirmReq.getRoomId(), userId);
            if (isValid == 0) {
                throw new BaseException(BAD_REQUEST);
            }

            tradeDao.insertTradeConfirm(postTradeConfirmReq, userId);
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
