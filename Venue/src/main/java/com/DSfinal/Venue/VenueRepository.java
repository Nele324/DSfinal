package com.DSfinal.Venue;

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class VenueRepository {

    private final CosmosContainer container;

    public VenueRepository(CosmosClient client) {

        CosmosDatabase database =
                client.getDatabase("venue-db");

        this.container =
                database.getContainer("venues");
    }

    public List<VenueHall> findAll() {

        List<VenueHall> list = new ArrayList<>();

        String query = "SELECT * FROM c";

        CosmosPagedIterable<VenueHall> items =
                container.queryItems(
                        query,
                        new CosmosQueryRequestOptions(),
                        VenueHall.class
                );

        items.forEach(list::add);

        return list;
    }

    public VenueHall findById(String id) {

        try {

            CosmosItemResponse<VenueHall> response =
                    container.readItem(
                            id,
                            new PartitionKey(id),
                            VenueHall.class
                    );

            return response.getItem();

        } catch (Exception e) {

            return null;
        }
    }

    public VenueHall save(VenueHall hall) {

        CosmosItemResponse<VenueHall> response =
                container.upsertItem(hall);

        return response.getItem();
    }
}