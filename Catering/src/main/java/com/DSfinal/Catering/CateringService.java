package com.DSfinal.Catering;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CateringService {

    private final CosmosContainer container;

    public CateringService(CosmosClient client) {
        this.container = client
                .getDatabase("catering-db")   // jouw database in Azure
                .getContainer("caterings");   // jouw container in Azure
    }

    public List<CateringOption> getAllCateringOptions() {
        String query = "SELECT * FROM c";

        return container.queryItems(query, new CosmosQueryRequestOptions(), CateringOption.class)
                .stream()
                .toList();
    }
}