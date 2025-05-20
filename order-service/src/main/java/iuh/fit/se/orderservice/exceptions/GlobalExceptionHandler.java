package iuh.fit.se.orderservice.exceptions;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle exceptions thrown by Feign clients via ResponseStatusException
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", ex.getStatusCode().value());
        
        // Lấy mô tả lỗi HTTP từ status code
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        body.put("error", status.getReasonPhrase());
        
        body.put("message", ex.getReason());

        return new ResponseEntity<>(body, ex.getStatusCode());
    }

    /**
     * Handle validation exceptions (e.g., @Valid failures)
     */
    @ExceptionHandler({ConstraintViolationException.class,
            MissingServletRequestParameterException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(Exception e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));

        String message = e.getMessage();
        if (e instanceof MethodArgumentNotValidException) {
            int start = message.lastIndexOf("[") + 1;
            int end = message.lastIndexOf("]") - 1;
            if (start > 0 && end > start) {
                message = message.substring(start, end);
            }
            errorResponse.setError("Invalid Payload");
            errorResponse.setMessage(message);
        } else if (e instanceof MissingServletRequestParameterException) {
            errorResponse.setError("Invalid Parameter");
            errorResponse.setMessage(message);
        } else if (e instanceof ConstraintViolationException) {
            errorResponse.setError("Invalid Parameter");
            if (message.indexOf(" ") > 0) {
                errorResponse.setMessage(message.substring(message.indexOf(" ") + 1));
            } else {
                errorResponse.setMessage(message);
            }
        } else {
            errorResponse.setError("Invalid Data");
            errorResponse.setMessage(message);
        }

        return errorResponse;
    }

    /**
     * Handle exceptions for resources not found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    public ErrorResponse handleResourceNotFoundException(ResourceNotFoundException e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
        errorResponse.setStatus(NOT_FOUND.value());
        errorResponse.setError(NOT_FOUND.getReasonPhrase());
        errorResponse.setMessage(e.getMessage());

        return errorResponse;
    }

    /**
     * Handle exceptions for invalid or conflicting data
     */
    @ExceptionHandler(InvalidDataException.class)
    @ResponseStatus(CONFLICT)
    public ErrorResponse handleInvalidDataException(InvalidDataException e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
        errorResponse.setStatus(CONFLICT.value());
        errorResponse.setError(CONFLICT.getReasonPhrase());
        errorResponse.setMessage(e.getMessage());

        return errorResponse;
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAllExceptions(Exception e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
        errorResponse.setStatus(INTERNAL_SERVER_ERROR.value());
        errorResponse.setError(INTERNAL_SERVER_ERROR.getReasonPhrase());
        errorResponse.setMessage("Unexpected error occurred");

        return errorResponse;
    }
}
