package iuh.fit.se.reviewservice.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Component
public class FeignErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder.Default defaultErrorDecoder = new Default();
    private final ObjectMapper objectMapper;

    @Autowired
    public FeignErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        try (InputStream bodyIs = response.body().asInputStream()) {
            Map<String, Object> error = objectMapper.readValue(bodyIs, Map.class);
            
            String message = error.containsKey("message") 
                ? (String) error.get("message") 
                : "Error calling " + methodKey;
                
            return new ResponseStatusException(
                HttpStatus.valueOf(response.status()),
                message
            );
        } catch (IOException e) {
            // Fallback to default handling if we can't parse the error response
            return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}
