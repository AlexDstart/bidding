package pro.sky.bidding.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class LotDTO implements Serializable {

    private int id;

    private String status;

    private String title;

    private String description;

    private int startPrice;

    private int bidPrice;

}
