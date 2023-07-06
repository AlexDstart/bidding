package pro.sky.bidding.exeption;

public class EmptyLot extends RuntimeException {
    public EmptyLot() {
        super("Lot is Empty");
    }
}

