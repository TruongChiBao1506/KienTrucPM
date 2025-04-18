package iuh.fit.se.reviewservice.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Configuration
@EnableElasticsearchRepositories(basePackages = "iuh.fit.se.reviewservice.repository.elasticsearch")
public class ElasticsearchConfig {
    // Let Spring Boot auto-configuration handle the Elasticsearch client setup
    // The properties in application.properties will be used automatically

    /**
     * Custom converter to handle conversion between String and LocalDateTime
     */
    @Bean
    public ElasticsearchCustomConversions elasticsearchCustomConversions() {
        return new ElasticsearchCustomConversions(
            Arrays.asList(
                new StringToLocalDateTimeConverter(),
                new LocalDateTimeToStringConverter()
            )
        );
    }

    /**
     * Converts String to LocalDateTime
     */
    static class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;
        private static final DateTimeFormatter BASIC_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public LocalDateTime convert(String source) {
            if (source == null || source.isEmpty()) {
                return null;
            }


            // First, try to directly parse the simple date format (yyyy-MM-dd)
            if (source.matches("\\d{4}-\\d{2}-\\d{2}")) {
                try {
                    // Parse using java.time.LocalDate directly which handles yyyy-MM-dd format well
                    return LocalDateTime.of(
                        java.time.LocalDate.parse(source),
                        java.time.LocalTime.MIDNIGHT
                    );
                } catch (Exception e) {
                    // If direct parsing fails, try with explicit formatters
                    try {
                        return LocalDateTime.of(
                            java.time.LocalDate.parse(source, BASIC_DATE_FORMATTER),
                            java.time.LocalTime.MIDNIGHT
                        );
                    } catch (Exception e2) {
                        try {
                            return LocalDateTime.of(
                                java.time.LocalDate.parse(source, DATE_FORMATTER),
                                java.time.LocalTime.MIDNIGHT
                            );
                        } catch (Exception e3) {
                            // Last attempt: try parsing with a more lenient approach
                            try {
                                String[] parts = source.split("-");
                                if (parts.length == 3) {
                                    int year = Integer.parseInt(parts[0]);
                                    int month = Integer.parseInt(parts[1]);
                                    int day = Integer.parseInt(parts[2]);
                                    return LocalDateTime.of(year, month, day, 0, 0, 0);
                                }
                            } catch (Exception e4) {
                                // Will be handled by the outer exception handler
                            }
                        }
                    }
                }
            }

            try {
                // Try parsing as ISO date-time format
                return LocalDateTime.parse(source, FORMATTER);
            } catch (Exception e) {
                try {
                    // If that fails, try parsing as ISO date format and set time to midnight
                    return LocalDateTime.parse(source + "T00:00:00", FORMATTER);
                } catch (Exception ex) {
                    try {
                        // If all else fails, try direct date parsing and set time to midnight
                        return LocalDateTime.of(
                            java.time.LocalDate.parse(source, DATE_FORMATTER), 
                            java.time.LocalTime.MIDNIGHT
                        );
                    } catch (Exception exc) {
                        // Last resort: try to handle any other format
                        throw new IllegalArgumentException("Cannot convert date value: " + source, exc);
                    }
                }
            }
        }
    }

    /**
     * Converts LocalDateTime to String
     */
    static class LocalDateTimeToStringConverter implements Converter<LocalDateTime, String> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

        @Override
        public String convert(LocalDateTime source) {
            return source == null ? null : source.format(FORMATTER);
        }
    }
}
