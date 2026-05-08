package com.DSfinal.broker;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Collections;
import java.util.List;

@Controller
public class BrokerViewController {

    //here we call the API Controller(Broker Controller) from the view controller
    //for this DS project, it is a clever shortcut
    //because in BrokerController, all the complex logics like
        //calling external services in different azure data centers
        //handling fault tolerance with try-catch blocks
        //simulating the administration domains
    //already exist
    //by injecting BrokerController into ViewController
    //we ensure that UI always shows exactly what the API sees
    //without duplicating all that networking code

    @Autowired
    private RestTemplate restTemplate;

    // These read the addresses from src/main/resources/application.properties
    @Value("${catering.service.url}")
    private String cateringServiceUrl;

    @Value("${venue.service.url}")
    private String venueServiceUrl;

    private final BrokerController apiController;

    // We inject the existing logic so we don't have to rewrite it
    public BrokerViewController(BrokerController apiController) {
        this.apiController = apiController;
    }

    @GetMapping("/")
    public String showIndex(Model model) {
        // Use the logic you already wrote to get data from suppliers
        AvailablePackagesResponse data = apiController.getAvailablePackages();

        // Put the data into the "bucket" for index.html to find
        model.addAttribute("venues", data.getVenues());
        model.addAttribute("cateringPackages", data.getCateringPackages());
        model.addAttribute("status", data.getStatus());

        return "index"; // This tells Spring to look for index.html
    }

    //this is a method to process the order
    //to satisfy ACID requirements
    //to ensure that if one part of the order fails the other is NOT finalized
    @PostMapping("/broker/confirm-order")
    public String placeOrder(OrderRequest request, Model model) {
        boolean venueReserved = false;
        boolean cateringReserved = false;

        try {
            // 1. Attempt to reserve Venue (Supplier A)
            // You should send a POST request to your venue service
            venueReserved = restTemplate.postForObject(venueServiceUrl + "venue/reserve/" + request.getSelectedVenue(), null, Boolean.class);

            // 2. Attempt to reserve Catering (Supplier B)
            cateringReserved = restTemplate.postForObject(cateringServiceUrl + "catering/reserve/" + request.getSelectedCatering(), null, Boolean.class);

            // 3. Check for Atomicity
            if (venueReserved && cateringReserved) {
                // SUCCESS: Both suppliers confirmed.

                //!!!!! here LOTTE save the order to Broker's Azure DB.
                // orderService.save(new Order(request...)); something like this maybe???

                model.addAttribute("message", "Order Placed Successfully!");
                return "order-success";
            } else {
                // FAILURE: One or both failed. Rollback/Undo if necessary.
                // (In a real system, you'd send a 'cancel' to whichever one succeeded)
                //!!!!! I still need to write code to cancel the successful reservation
                throw new Exception("One or more services unavailable.");
            }

        } catch (Exception e) {
            // 4. Handle Failure Scenario
            model.addAttribute("error", "Transaction Failed: " + e.getMessage());
            return "order-failed";
        }
    }

}
