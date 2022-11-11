package org.cmccx.src.trade;

import org.cmccx.config.BaseException;
import org.cmccx.config.BaseResponse;
import org.cmccx.src.trade.model.GetPurchaseHistoryRes;
import org.cmccx.src.trade.model.PostTradeConfirmReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/trades")
public class TradeController {
    private final TradeProvider tradeProvider;
    private final TradeService tradeService;

    @Autowired
    public TradeController(TradeProvider tradeProvider, TradeService tradeService) {
        this.tradeProvider = tradeProvider;
        this.tradeService = tradeService;
    }

    /**
     * 구매 확정 API
     * [GET] /trades
     * @return BaseResponse<GetPurchaseHistoryRes>
     */
    @ResponseBody
    @GetMapping("")
    public BaseResponse<GetPurchaseHistoryRes> getPurchaseHistory() throws BaseException {
        GetPurchaseHistoryRes result = tradeProvider.getPurchaseHistory();

        return new BaseResponse<>(result);
    }

    /**
     * 거래 확정 API
     * [POST] /trades
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("")
    public BaseResponse<String> registerTradeConfirm(@RequestBody @Valid PostTradeConfirmReq postTradeConfirmReq) throws BaseException {
        String result = tradeService.registerTradeConfirm(postTradeConfirmReq);

        return new BaseResponse<>(result);
    }

}
