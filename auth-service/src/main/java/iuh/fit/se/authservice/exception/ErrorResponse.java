package iuh.fit.se.authservice.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ErrorResponse {
    private Date timestamp;
    private int status;
    private String path;
    private String error;
    private String message;

    public Date getTimestamp() {
        return timestamp;
    }


}