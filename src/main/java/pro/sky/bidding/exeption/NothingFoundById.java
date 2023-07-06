package pro.sky.bidding.exeption;

public class NothingFoundById extends RuntimeException{
    public NothingFoundById(int id) {
        super("No instances for id: " + id);
    }
}
