package iuh.fit.se.productservice.exceptions;

public class RateLimitExceededException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final String limitType;
    
    public RateLimitExceededException(String message, String limitType) {
        super(message);
        this.limitType = limitType;
    }
    
    public String getLimitType() {
        return limitType;
    }
}
