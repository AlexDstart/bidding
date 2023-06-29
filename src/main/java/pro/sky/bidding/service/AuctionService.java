package pro.sky.bidding.service;

import org.springframework.data.domain.Page;
import pro.sky.bidding.dto.CreateBidDTO;
import pro.sky.bidding.dto.CreateLotDTO;
import pro.sky.bidding.dto.FullLotDTO;
import pro.sky.bidding.dto.LotDTO;
import pro.sky.bidding.enums.LotStatus;

public interface AuctionService {

    String getFirstBidder(int lotId);

    String getMostFrequentBidder(int lotId);

    FullLotDTO getFullLotById(int lotId);

    boolean startLot(int lotId);

    String createBid(int lotId, CreateBidDTO createBidDTO);

    boolean stopLot(int lotId);

    LotDTO createLot(CreateLotDTO lotRequest);

    Page<LotDTO> findLotsByStatus(LotStatus status, int page);

    byte[] exportLotsToCSV();
}

