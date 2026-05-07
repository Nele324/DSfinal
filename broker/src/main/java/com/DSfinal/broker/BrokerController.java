package com.DSfinal.broker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    //@value reads a value from your application.properties file and
    //injects it into a variable.
    //We don’t hardcode this
    //so that later, we only change ‘application.properties’ to point to Azure
    @Value("${catering.service.url}")
    private String cateringServiceUrl;

    @Value("${venue.service.url}")
    private String venueServiceUrl;

    @GetMapping("/all-combinations")
    public List<CombinedOption> getAllCombinations() {
        // Gebruik de variabelen uit application.properties in plaats van hardcoded "localhost"
        // Let op: zorg dat de paden (/venue/halls) overeenkomen met de Controllers in die services
        VenueHall[] venues = restTemplate.getForObject(venueServiceUrl + "/venue/halls", VenueHall[].class);
        CateringPackage[] caterings = restTemplate.getForObject(cateringServiceUrl + "/catering/packages", CateringPackage[].class);

        List<CombinedOption> combinations = new ArrayList<>();

        if (venues != null && caterings != null) {
            for (VenueHall v : venues) {
                System.out.println("Checking Venue: " + v.getName() + " Price: " + v.getPricePerDay());
                for (CateringPackage c : caterings) {
                    System.out.println("Checking Catering: " + c.getName() + " Price: " + c.getPricePerPerson();
                    combinations.add(new CombinedOption(v, c));
                }
            }
        }
        return combinations;
    }

    @GetMapping("/available-packages")
    public AvailablePackagesResponse getAvailablePackages() {

        // Call Catering Service
        List<Map<String, Object>> cateringPackages;
        //added a try/catch block
        //in case either of the suppliers are unresponsive or return a faulty response
        //broker doesn't crash
        try {
            ResponseEntity<List<Map<String, Object>>> cateringResponse = restTemplate.exchange(
                    cateringServiceUrl + "/catering/packages",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            cateringPackages = cateringResponse.getBody();
        } catch (RestClientException e) {
            // Catering service is down — broker stays up, just reports it
            System.err.println("Catering service unavailable: " + e.getMessage());
            cateringPackages = Collections.emptyList();
        }

        // Call Venue Service
        List<Map<String, Object>> venues;
        try {
            ResponseEntity<List<Map<String, Object>>> venueResponse = restTemplate.exchange(
                    venueServiceUrl + "/venue/halls",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            venues = venueResponse.getBody();
        } catch (RestClientException e) {
            // Venue service is down — broker stays up, just reports it
            System.err.println("Venue service unavailable: " + e.getMessage());
            venues = Collections.emptyList();
        }

        // Combine and return
        String status = (cateringPackages.isEmpty() || venues.isEmpty())
                ? "PARTIAL - one or more suppliers unavailable"
                : "OK - all suppliers responding";

        return new AvailablePackagesResponse(venues, cateringPackages, status); //currently broker has a list of
        //available catering services and venues side by side,
        //they are not being made into a combo the user will order as a final product
        //that combo is created only when the user chooses one specific caterer and one specific venue successfully
        //then, we make a final combo
    }
}