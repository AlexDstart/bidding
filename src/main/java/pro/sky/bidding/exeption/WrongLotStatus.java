package pro.sky.bidding.exeption;

public class WrongLotStatus extends RuntimeException {
    public WrongLotStatus() {
        super("Wrong Lot Status");
    }
}
