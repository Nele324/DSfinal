package com.DSfinal.broker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/broker")
public class BrokerController {

    private static final Logger log = LoggerFactory.getLogger(BrokerController.class);

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

    @PreAuthorize("hasAuthority('MANAGER')")
    @GetMapping("/orders")
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    //this is just putting the order in broker's database
    @PostMapping("/place-order")
    public ResponseEntity<String> placeOrder(@RequestBody Order orderRequest) {
        log.info("Received order request: {}", orderRequest);
        try {
            orderRepository.save(orderRequest);
            return ResponseEntity.ok("Order succesvol opgeslagen met ID: " + orderRequest.getId());
        } catch (Exception e) {
            log.error("Fout bij opslaan order", e);
            return ResponseEntity.status(500).body("Fout bij opslaan order: " + e.getMessage());
        }
    }


    //this is getting the available packages from the suppliers
    @GetMapping("/available-packages")
    public AvailablePackagesResponse getAvailablePackages(String date) {

        List<CateringPackage> cateringPackages;

        try {

            CateringPackage[] catArray =
                    restTemplate.getForObject(
                            cateringServiceUrl +
                                    "/catering/options?date=" + date,
                            CateringPackage[].class
                    );

            cateringPackages = (catArray != null)
                    ? Arrays.asList(catArray)
                    : Collections.emptyList();

            /*
            log.info("Catering packages ontvangen: {}", cateringPackages.size());
            for (CateringPackage c : cateringPackages) {
                log.info("  - Catering: ID={}, Naam={}, MaxGuests={}, Prijs per persoon: ${}", 
                    c.getId(), c.getName(), c.getMaxGuests(), c.getPricePerPerson());
            }
            */

        } catch (Exception e) {

            System.err.println("Catering error: " + e.getMessage());
            log.error("Fout bij ophalen catering packages", e);

            cateringPackages = Collections.emptyList();
        }

        List<VenueHall> venues;

        try {

            VenueHall[] venArray =
                    restTemplate.getForObject(
                            venueServiceUrl +
                                    "/venue/halls?date=" + date,
                            VenueHall[].class
                    );

            venues = (venArray != null)
                    ? Arrays.asList(venArray)
                    : Collections.emptyList();

            /*
            log.info("Venues ontvangen: {}", venues.size());
            for (VenueHall v : venues) {
                log.info("  - Venue: ID={}, Naam={}, Capaciteit={}, Prijs per dag: ${}", 
                    v.getId(), v.getName(), v.getCapacity(), v.getPricePerDay());
            }
            */

        } catch (Exception e) {

            System.err.println("Venue error: " + e.getMessage());
            log.error("Fout bij ophalen venues", e);

            venues = Collections.emptyList();
        }

        String status =
                (cateringPackages.isEmpty() || venues.isEmpty())
                        ? "PARTIAL - one or more suppliers unavailable"
                        : "OK - all suppliers responding";

        log.info("Status: {}", status);

        return new AvailablePackagesResponse(
                venues,
                cateringPackages,
                status
        );
    }


    //this is broker reserving a venue to the supplier
    public boolean reserveVenue(String id, String date) {

        try {

            Map<String, Object> request = Map.of(
                    "venueId", id,
                    "date", date
            );

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            venueServiceUrl + "/venue/reserve",
                            request,
                            Map.class
                    );

            Map body = response.getBody();

            return body != null &&
                    Boolean.TRUE.equals(body.get("success"));

        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            log.error("HTTP Fout van Venue Service: Status {} - Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            log.error("Onverwachte fout tijdens reserveVenue: ", e);
            return false;
        }
    }

    //this is broker reserving a catering service to the supplier
    public boolean reserveCatering(String id, String date) {

        try {

            Map<String, Object> request = Map.of(
                    "cateringId", id,
                    "date", date
            );

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            cateringServiceUrl + "/catering/reserve",
                            request,
                            Map.class
                    );

            Map body = response.getBody();

            return body != null &&
                    Boolean.TRUE.equals(body.get("success"));

        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            log.error("HTTP Fout van Catering Service: Status {} - Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            log.error("Onverwachte fout tijdens reserveCatering: ", e);
            return false;
        }
    }

    public boolean confirmCatering(String id, String date) {
        try {
            String url = cateringServiceUrl + "/catering/confirm?cateringId=" + id + "&date=" + date;

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            url,
                            null,
                            Map.class
                    );

            Map body = response.getBody();

            return body != null &&
                    Boolean.TRUE.equals(body.get("success"));

        } catch (Exception e) {
            log.error("Fout tijdens het bevestigen van de catering: " + e.getMessage());
            return false;
        }
    }

    public boolean confirmVenue(String id, String date) {
        try {
            String url = venueServiceUrl + "/venue/confirm?venueId=" + id + "&date=" + date;

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            url,
                            null,
                            Map.class
                    );

            Map body = response.getBody();

            return body != null &&
                    Boolean.TRUE.equals(body.get("success"));

        } catch (Exception e) {
            log.error("Fout tijdens het bevestigen van de venue: " + e.getMessage());
            return false;
        }
    }

    // Compensating transactions (Saga pattern) - cancel methods for rollback
    public boolean cancelVenue(String id, String date) {
        try {
            String url = venueServiceUrl + "/venue/cancel?venueId=" + id + "&date=" + date;

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            url,
                            null,
                            Map.class
                    );

            Map body = response.getBody();
            boolean cancelled = body != null && Boolean.TRUE.equals(body.get("success"));
            
            if (cancelled) {
                log.info("Venue {} cancelled for date {} as compensating transaction", id, date);
            } else {
                log.error("Failed to cancel venue {} for date {} - rollback may be incomplete", id, date);
            }
            
            return cancelled;

        } catch (Exception e) {
            log.error("Error during venue cancellation (compensating transaction): " + e.getMessage());
            return false;
        }
    }

    // Compensating transaction for catering
    public boolean cancelCatering(String id, String date) {
        try {
            String url = cateringServiceUrl + "/catering/cancel?cateringId=" + id + "&date=" + date;

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            url,
                            null,
                            Map.class
                    );

            Map body = response.getBody();
            boolean cancelled = body != null && Boolean.TRUE.equals(body.get("success"));
            
            if (cancelled) {
                log.info("Catering {} cancelled for date {} as compensating transaction", id, date);
            } else {
                log.error("Failed to cancel catering {} for date {} - rollback may be incomplete", id, date);
            }
            
            return cancelled;

        } catch (Exception e) {
            log.error("Error during catering cancellation (compensating transaction): " + e.getMessage());
            return false;
        }
    }

    // Release methods for reservations (SAGA compensating transactions for reservation phase)
    // These are called when a reservation needs to be rolled back
    
    public boolean releaseVenue(String id, String date) {
        try {
            Map<String, Object> request = Map.of(
                    "venueId", id,
                    "date", date
            );
            
            String url = venueServiceUrl + "/venue/cancel?venueId=" + id + "&date=" + date;
            
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            url,
                            null,
                            Map.class
                    );
            
            Map body = response.getBody();
            boolean released = body != null && Boolean.TRUE.equals(body.get("success"));
            
            if (released) {
                log.info("Venue {} released for date {} (reservation phase rollback)", id, date);
            } else {
                log.error("Failed to release venue {} for date {} - reservation may still be pending", id, date);
            }
            
            return released;
            
        } catch (Exception e) {
            log.error("Error during venue release (compensating transaction for reservation): " + e.getMessage());
            return false;
        }
    }

    public boolean releaseCatering(String id, String date) {
        try {
            Map<String, Object> request = Map.of(
                    "cateringId", id,
                    "date", date
            );
            
            String url = cateringServiceUrl + "/catering/cancel?cateringId=" + id + "&date=" + date;
            
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            url,
                            null,
                            Map.class
                    );
            
            Map body = response.getBody();
            boolean released = body != null && Boolean.TRUE.equals(body.get("success"));
            
            if (released) {
                log.info("Catering {} released for date {} (reservation phase rollback)", id, date);
            } else {
                log.error("Failed to release catering {} for date {} - reservation may still be pending", id, date);
            }
            
            return released;
            
        } catch (Exception e) {
            log.error("Error during catering release (compensating transaction for reservation): " + e.getMessage());
            return false;
        }
    }
}