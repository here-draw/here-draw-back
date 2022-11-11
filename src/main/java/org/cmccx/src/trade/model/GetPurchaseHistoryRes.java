package org.cmccx.src.trade.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.cmccx.src.art.model.ArtInfo;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetPurchaseHistoryRes {
    private int count;
    private List<ArtInfo> artList;
}
