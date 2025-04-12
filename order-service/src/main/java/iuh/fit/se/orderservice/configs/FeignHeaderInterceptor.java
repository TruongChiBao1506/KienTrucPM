package iuh.fit.se.orderservice.configs;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignHeaderInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String user = request.getHeader("X-Auth-User");
            String roles = request.getHeader("X-Auth-Roles");

            if (user != null && roles != null) {
                requestTemplate.header("X-Auth-User", user);
                requestTemplate.header("X-Auth-Roles", roles);
            }
        }
    }
}
