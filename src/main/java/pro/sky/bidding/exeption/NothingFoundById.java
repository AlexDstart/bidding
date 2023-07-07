package pro.sky.bidding.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class NothingFoundById extends RuntimeException{
    public NothingFoundById(int id) {
        super("No instances for id: " + id);
    }
}
