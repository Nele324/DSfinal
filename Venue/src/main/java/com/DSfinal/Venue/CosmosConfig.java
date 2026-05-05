package com.DSfinal.Venue;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CosmosConfig {

    @Bean
    public CosmosClient cosmosClient() {
        return new CosmosClientBuilder()
                .endpoint(System.getenv("COSMOS_ENDPOINT"))
                .key(System.getenv("COSMOS_KEY"))
                .buildClient();
    }
}