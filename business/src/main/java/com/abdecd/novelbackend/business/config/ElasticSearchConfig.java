package com.abdecd.novelbackend.business.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(prefix = "spring.data.elasticsearch", name = "url")
@Configuration
public class ElasticSearchConfig {
    @Value("${spring.data.elasticsearch.url}")
    private String serverUrl;

    @Bean
    public ElasticsearchClient esClient() {
        RestClient restClient = RestClient
                .builder(HttpHost.create(serverUrl))
                .build();

        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }
}
