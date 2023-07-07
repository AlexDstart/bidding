package pro.sky.bidding.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pro.sky.bidding.enums.LotStatus;
import pro.sky.bidding.model.Lot;

public interface LotRepository extends JpaRepository<Lot, Integer> {
    Page<Lot> findAllByStatus(LotStatus status, Pageable pageable);
}