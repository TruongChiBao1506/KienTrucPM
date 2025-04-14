package iuh.fit.se.reviewservice.exceptions;

/**
 * Exception for invalid or conflicting data
 */
public class InvalidDataException extends RuntimeException {
    public InvalidDataException(String message) {
        super(message);
    }
}
