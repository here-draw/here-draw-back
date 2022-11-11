package org.cmccx.src.trade;

import org.cmccx.config.BaseException;
import org.cmccx.src.art.model.ArtInfo;
import org.cmccx.src.trade.model.GetPurchaseHistoryRes;
import org.cmccx.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.cmccx.config.BaseResponseStatus.DATABASE_ERROR;
import static org.cmccx.config.BaseResponseStatus.RESPONSE_ERROR;

@Service
public class TradeProvider {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TradeDao tradeDao;
    private final JwtService jwtService;

    @Autowired
    public TradeProvider(TradeDao tradeDao, JwtService jwtService) {
        this.tradeDao = tradeDao;
        this.jwtService = jwtService;
    }

    /** 구매 내역 조회 **/
    public GetPurchaseHistoryRes getPurchaseHistory() throws BaseException {
        try {
            //회원 검증 및 ID 추출
            long userId = jwtService.getUserId();

            List<ArtInfo> artList = tradeDao.selectPurchaseHistory(userId);
            GetPurchaseHistoryRes result = new GetPurchaseHistoryRes(artList.size(), artList);

            return result;
        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("GetPurchaseHistory", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /** 거래 상태 확인 **/
    public String getTradeStatus(long roomId) throws BaseException {
        try {
            return tradeDao.selectTradeStatus(roomId);
        } catch (Exception e) {
            logger.error("GetTradeStatus", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
