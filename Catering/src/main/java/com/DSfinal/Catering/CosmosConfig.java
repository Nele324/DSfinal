package com.DSfinal.Catering;

import com.azure.cosmos.*;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CosmosConfig {

    @Bean
    public CosmosClient cosmosClient() {
        return new CosmosClientBuilder()
                .endpoint(System.getenv("COSMOS_ENDPOINT_NELE"))
                .key(System.getenv("COSMOS_KEY_NELE"))
                .gatewayMode()
                .buildClient();
    }
}