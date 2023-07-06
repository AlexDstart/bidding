package pro.sky.bidding.exeption;

public class NoParticipant extends RuntimeException{
    public NoParticipant() {
        super("Участник не найден");
    }
}

