package iuh.fit.se.userservice.configs;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.util.Base64;


@Configuration
@EnableElasticsearchRepositories(basePackages = "iuh.fit.se.userservice.repositories")
public class ElasticsearchConfig {
    @Bean
    public RestClient restClient() {
        return RestClient.builder(HttpHost.create("http://localhost:9200"))
                .setDefaultHeaders(new Header[]{
                        new BasicHeader("Authorization", "Basic " + Base64.getEncoder()
                                .encodeToString("elastic:123456".getBytes()))
                })
                .build();
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }

    @Bean
    public ElasticsearchTemplate elasticsearchTemplate(ElasticsearchClient client) {
        return new ElasticsearchTemplate(client);
    }

    @Bean
    public ElasticsearchOperations elasticsearchOperations(ElasticsearchTemplate elasticsearchTemplate) {
        return elasticsearchTemplate;
    }

}
