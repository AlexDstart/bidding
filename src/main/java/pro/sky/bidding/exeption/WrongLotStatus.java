package pro.sky.bidding.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class WrongLotStatus extends RuntimeException {
    public WrongLotStatus() {
        super("Wrong Lot Status");
    }
}
