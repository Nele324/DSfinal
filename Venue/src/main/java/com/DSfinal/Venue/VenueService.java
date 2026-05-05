package com.DSfinal.Venue;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VenueService {

    private final CosmosContainer container;

    public VenueService(CosmosClient client) {
        this.container = client
                .getDatabase("venue-db")   // jouw database in Azure
                .getContainer("venues");   // jouw container in Azure
    }

    public List<VenueHall> getAllVenues() {
        String query = "SELECT * FROM c";

        return container.queryItems(query, new CosmosQueryRequestOptions(), VenueHall.class)
                .stream()
                .toList();
    }
}