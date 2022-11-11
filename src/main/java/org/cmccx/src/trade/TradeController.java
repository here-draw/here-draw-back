package org.cmccx.src.trade;

import org.cmccx.config.BaseException;
import org.cmccx.config.BaseResponse;
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
     * 거래 확정 API
     * [POST] /trades/{roomId}
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("")
    public BaseResponse<String> registerTradeConfirm(@RequestBody @Valid PostTradeConfirmReq postTradeConfirmReq) throws BaseException {
        String result = tradeService.registerTradeConfirm(postTradeConfirmReq);

        return new BaseResponse<>(result);
    }

}
