package pro.sky.bidding.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NO_CONTENT)
public class NoParticipant extends RuntimeException{
    public NoParticipant() {
        super("Участник не найден");
    }
}

