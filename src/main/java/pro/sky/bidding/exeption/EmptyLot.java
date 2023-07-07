package pro.sky.bidding.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class EmptyLot extends RuntimeException {
    public EmptyLot() {
        super("Lot is Empty");
    }
}

