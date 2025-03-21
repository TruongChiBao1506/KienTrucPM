package iuh.fit.se.userservice.configs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchClientConfig {
    private static final Logger logger = LogManager.getLogger(ElasticsearchClientConfig.class);

    public ElasticsearchClientConfig() {
        enableDebugLogging();
    }

    private void enableDebugLogging() {
        System.setProperty("org.apache.logging.log4j.simplelog.StatusLogger.level", "TRACE");
        logger.debug("Debug logging enabled for Elasticsearch client");
    }
}
