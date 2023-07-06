package pro.sky.bidding.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pro.sky.bidding.dto.*;
import pro.sky.bidding.enums.LotStatus;
import pro.sky.bidding.exeption.EmptyLot;
import pro.sky.bidding.exeption.NothingFoundById;
import pro.sky.bidding.exeption.WrongLotStatus;
import pro.sky.bidding.model.Bid;
import pro.sky.bidding.model.Lot;
import pro.sky.bidding.repository.BidRepository;
import pro.sky.bidding.repository.LotRepository;
import pro.sky.bidding.utilities.ServiceUtilities;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuctionServiceImpl implements AuctionService {

    private final LotRepository lotRepository;
    private final BidRepository bidRepository;
    private final ModelMapper modelMapper;
    private final ServiceUtilities serviceUtilities;
    private static final Logger logger = LoggerFactory.getLogger(AuctionServiceImpl.class);

    public AuctionServiceImpl(LotRepository lotRepository, BidRepository bidRepository, ModelMapper modelMapper, ServiceUtilities serviceUtilities) {
        this.lotRepository = lotRepository;
        this.bidRepository = bidRepository;
        this.modelMapper = modelMapper;
        this.serviceUtilities = serviceUtilities;
    }

    @Override
    @Cacheable("firstBidder")
    public String getFirstBidder(int lotId) {
        Lot lot = lotRepository.findById(lotId).orElse(null);
        if(lot==null) {
        throw new NothingFoundById(lotId);
        }
        if (lot.getStatus() == LotStatus.CREATED) {
            throw new WrongLotStatus();
        }
        Optional<Bid> firstBidOptional = lot.getBidsById().stream().findFirst();
        if (firstBidOptional.isEmpty()) {
            throw new EmptyLot();
        }
        Bid firstBid = firstBidOptional.get();
        BidDTO firstBidder = new BidDTO();
        firstBidder.setBidderName(firstBid.getBidderName());
        firstBidder.setBidTime(firstBid.getBidTime());
        return firstBidder.toString();
    }

    @Override
    @Cacheable("mostFrequentBidder")
    public String getMostFrequentBidder(int lotId) {
        logger.info("Запущен метод getMostFrequentBidder");
        Lot lot = lotRepository.findById(lotId).orElse(null);
        if (lot == null) {
            throw new NothingFoundById(lotId);

        }
        List<Bid> bids = lot.getBidsById();
        if (bids.isEmpty()) {
            throw new EmptyLot();
        }
        Map<String, Long> bidderCounts = bids.stream()
                .collect(Collectors.groupingBy(Bid::getBidderName, Collectors.counting()));
        logger.info("Количество ставок для каждого участника - bidderCounts: " + bidderCounts);
        long maxBidCount = bidderCounts.values().stream()
                .max(Long::compare)
                .orElse(0L);
        logger.info("Максимальное количество ставок - maxBidCount: " + maxBidCount);
        List<String> mostFrequentBidders = bidderCounts.entrySet().stream()
                .filter(entry -> entry.getValue() == maxBidCount)
                .map(Map.Entry::getKey)
                .toList();
        logger.info("Участник с максимальным количеством ставок - mostFrequentBidders: " + mostFrequentBidders);

        if (mostFrequentBidders.size() != 1) {
            return "Не удалось определить наиболее активного участника";
        }
        String mostFrequentBidderName = mostFrequentBidders.get(0);
        Optional<Bid> mostFrequentBidderLastBid = bids.stream()
                .filter(bid -> bid.getBidderName().equals(mostFrequentBidderName))
                .max(Comparator.comparing(Bid::getBidTime));
        BidDTO mostFrequentBidder = new BidDTO();
        mostFrequentBidder.setBidderName(mostFrequentBidderName);
        mostFrequentBidder.setBidTime(mostFrequentBidderLastBid.get().getBidTime());
        return mostFrequentBidder.toString();
    }

    @Override
    @Cacheable("fullLot")
    public FullLotDTO getFullLotById(int lotId) {
        Lot lot = lotRepository.findById(lotId).orElse(null);
        FullLotDTO fullLotDTO = new FullLotDTO();
        if (lot != null) {
            modelMapper.map(lot, fullLotDTO);
            fullLotDTO.setCurrentPrice(lot.getStatus() != LotStatus.CREATED ? serviceUtilities.calculateCurrentPrice(lot) : lot.getStartPrice());
            fullLotDTO.setLastBid(serviceUtilities.getLastBidDTO(lot));
        }
        return fullLotDTO;
    }

    @Override
    public boolean startLot(int lotId) {
        logger.info("Запущен  startLot");
        try {
            Lot lot = lotRepository.findById(lotId).orElseThrow();
            if (lot.getStatus() != LotStatus.STARTED) {
                lot.setStatus(LotStatus.STARTED);
                lotRepository.save(lot);
            }
        } catch (Exception e) {
            logger.error("Ошибка чтения/записи в БД ");
            return false;
        }
        return true;
    }

    @Override
    @Cacheable("createBid")
    public String createBid(int lotId, CreateBidDTO createBidDTO) {
        logger.info("Запущен метод createBid");
        Lot lot = lotRepository.findById(lotId).orElse(null);
        logger.debug("Обращение к таблице lot , результат - lot: " + lot);
        if (lot == null) {
            throw new NothingFoundById(lotId);
        }
        if (lot.getStatus() != LotStatus.STARTED) {
            return "Лот в неверном статусе";
        }
        Bid bid = new Bid();
        bid.setBidderName(createBidDTO.getBidderName());
        bid.setBidTime(Timestamp.valueOf(LocalDateTime.now().plusHours(4)));
        bid.setLotByLotId(lot);
        bidRepository.save(bid);
        logger.debug("Обращение к таблице bid (запись), результат - bid: " + bid);
        return "Ставка создана";
    }

    @Override
    public boolean stopLot(int lotId) {
        logger.info("Запущен метод stopLot");
        try {
            Lot lot = lotRepository.findById(lotId).orElseThrow();
            if (lot.getStatus() != LotStatus.STOPPED) {
                lot.setStatus(LotStatus.STOPPED);
                lotRepository.save(lot);
            }
        } catch (Exception e) {
            logger.error("Ошибка чтения/записи в БД ");
            return false;
        }
        return true;
    }

    @Override
    public LotDTO createLot(CreateLotDTO createLotDTO) {
        logger.info("Запущен метод createLot");
        Lot lot =  modelMapper.map(createLotDTO, Lot.class);
        lot.setStatus(LotStatus.CREATED);
        lotRepository.save(lot);
        logger.debug("Обращение к таблице lot (запись), результат - lot: " + lot);
        return modelMapper.map(lot, LotDTO.class);
    }

    @Override
    @Cacheable("lotsByStatus")
    public Page<LotDTO> findLotsByStatus(LotStatus status, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Lot> lotPage = lotRepository.findAllByStatus(status, pageable);
        if (lotPage.isEmpty()) {
            logger.info("Нет данных ");
            return Page.empty();
        }
        return lotPage.map(lot -> modelMapper.map(lot, LotDTO.class));
    }

    @Override
    @Cacheable("exportLotsToCSV")
    public byte[] exportLotsToCSV() {
        logger.info("Запущен метод exportLotsToCSV ");
        List<Lot> lots = lotRepository.findAll();
        logger.debug("Обращение к таблице lot , результат - lots: " + lots);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.POSTGRESQL_CSV)) {
            csvPrinter.printRecord("id", "title", "status", "lastBidder", "currentPrice");
            lots.stream()
                    .map(lot -> Arrays.asList(
                            lot.getId(),
                            lot.getTitle(),
                            lot.getStatus(),
                            lot.getBidsById().isEmpty() ? "нет ставок" : lot.getBidsById().get(lot.getBidsById().size() - 1).getBidderName(),
                            serviceUtilities.calculateCurrentPrice(lot)
                    ))
                    .forEach(record -> {
                        try {
                            csvPrinter.printRecord(record);
                        } catch (IOException e) {
                            e.printStackTrace();
                            logger.error("Ошибка I/O в csvPrinter");
                        }
                    });
            csvPrinter.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Ошибка I/O в потоке");
            return new byte[0];
        }
    }
}
