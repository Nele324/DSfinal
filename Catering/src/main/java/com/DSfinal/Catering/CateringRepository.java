package com.DSfinal.Catering;

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import org.springframework.stereotype.Repository;
import com.azure.cosmos.util.CosmosPagedIterable;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CateringRepository {

    private final CosmosContainer container;

    public CateringRepository(CosmosClient client) {

        CosmosDatabase database =
                client.getDatabase("catering-db");

        this.container =
                database.getContainer("caterings");
    }

    public List<CateringOption> findAll() {

        List<CateringOption> list = new ArrayList<>();

        String query = "SELECT * FROM c";

        CosmosPagedIterable<CateringOption> items =
                container.queryItems(
                        query,
                        new CosmosQueryRequestOptions(),
                        CateringOption.class
                );

        items.forEach(list::add);

        return list;
    }

    public CateringOption findById(String id) {

        try {

            CosmosItemResponse<CateringOption> response =
                    container.readItem(
                            id,
                            new PartitionKey(id),
                            CateringOption.class
                    );

            return response.getItem();

        } catch (Exception e) {

            return null;
        }
    }

    public CateringOption save(CateringOption option) {

        CosmosItemResponse<CateringOption> response =
                container.upsertItem(option);

        return response.getItem();
    }
}