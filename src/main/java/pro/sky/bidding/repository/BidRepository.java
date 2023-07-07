package pro.sky.bidding.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.sky.bidding.model.Bid;

public interface BidRepository extends JpaRepository<Bid, Integer> {
}
