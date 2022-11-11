package org.cmccx.src.trade;

import org.cmccx.config.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.cmccx.config.BaseResponseStatus.DATABASE_ERROR;

@Service
public class TradeProvider {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TradeDao tradeDao;

    @Autowired
    public TradeProvider(TradeDao tradeDao) {
        this.tradeDao = tradeDao;
    }

    /** 거래 상태 확인 **/
    public String getTradeStatus(long roomId) throws BaseException {
        try {
            return tradeDao.getTradeStatus(roomId);
        } catch (Exception e) {
            logger.error("GetTradeStatus", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
