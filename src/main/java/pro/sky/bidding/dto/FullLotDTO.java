package pro.sky.bidding.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import pro.sky.bidding.enums.LotStatus;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class FullLotDTO implements Serializable {
    private int id;

    private LotStatus status;

    private String title;

    private String description;

    private int startPrice;

    private int bidPrice;

    private int currentPrice;

    private BidDTO lastBid;
}