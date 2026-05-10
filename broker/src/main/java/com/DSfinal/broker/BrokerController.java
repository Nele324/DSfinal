package com.DSfinal.broker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;

@RestController
@RequestMapping("/broker")
public class BrokerController {

    //@autowired it to get the RestTemplate Bean object we created
    //in RestTemplateConfig.java
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OrderRepository orderRepository;

    //@value reads a value from your application.properties file and
    //injects it into a variable.
    //We don’t hardcode this
    //so that later, we only change ‘application.properties’ to point to Azure
    @Value("${catering.service.url}")
    private String cateringServiceUrl;

    @Value("${venue.service.url}")
    private String venueServiceUrl;

    // I am not sure why we need to get all combinations
    @GetMapping("/all-combinations")
    public List<CombinedOption> getAllCombinations() {
        // Gebruik de variabelen uit application.properties in plaats van hardcoded "localhost"
        // Let op: zorg dat de paden (/venue/halls) overeenkomen met de Controllers in die services
        VenueHall[] venues = restTemplate.getForObject(venueServiceUrl + "/venue/halls", VenueHall[].class);
        CateringPackage[] caterings = restTemplate.getForObject(cateringServiceUrl + "/catering/options", CateringPackage[].class);

        List<CombinedOption> combinations = new ArrayList<>();

        if (venues != null && caterings != null) {
            for (VenueHall v : venues) {
                for (CateringPackage c : caterings) {
                    combinations.add(new CombinedOption(v, c));
                }
            }
        }
        return combinations;
    }

    @GetMapping("/orders")
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    //this is just putting the order in broker's database
    @PostMapping("/place-order")
    public ResponseEntity<String> placeOrder(@RequestBody Order orderRequest) {
        try {
            // We slaan het object direct op in Azure SQL
            orderRepository.save(orderRequest);
            return ResponseEntity.ok("Order succesvol opgeslagen met ID: " + orderRequest.getId());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Fout bij opslaan order: " + e.getMessage());
        }
    }


    //this is getting the available packages from the suppliers
    @GetMapping("/available-packages")
    public AvailablePackagesResponse getAvailablePackages() {

        // 1. Call Catering Service using the Class instead of a Map
        List<CateringPackage> cateringPackages;
        try {
            CateringPackage[] catArray = restTemplate.getForObject(
                    cateringServiceUrl + "/catering/options",
                    CateringPackage[].class
            );
            cateringPackages = (catArray != null) ? Arrays.asList(catArray) : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Catering error: " + e.getMessage());
            cateringPackages = Collections.emptyList();
        }

        // 2. Call Venue Service using the Class
        List<VenueHall> venues;
        try {
            VenueHall[] venArray = restTemplate.getForObject(
                    venueServiceUrl + "/venue/halls",
                    VenueHall[].class
            );
            venues = (venArray != null) ? Arrays.asList(venArray) : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Venue error: " + e.getMessage());
            venues = Collections.emptyList();
        }

        String status = (cateringPackages.isEmpty() || venues.isEmpty())
                ? "PARTIAL - one or more suppliers unavailable"
                : "OK - all suppliers responding";

        return new AvailablePackagesResponse(venues, cateringPackages, status);
    }


    //this is broker reserving a venue to the supplier
    public boolean reserveVenue(String id) {
        try {
            return restTemplate.postForObject(venueServiceUrl + "/venue/reserve/" + id, null, Boolean.class);
        } catch (Exception e) {
            return false;
        }
    }

    //this is broker reserving a catering service to the supplier
    public boolean reserveCatering(String id) {
        try {
            return restTemplate.postForObject(cateringServiceUrl + "/catering/reserve/" + id, null, Boolean.class);
        } catch (Exception e) {
            return false;
        }
    }
}