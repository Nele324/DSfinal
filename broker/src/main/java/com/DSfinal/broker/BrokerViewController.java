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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Collections;
import java.util.List;
import java.time.LocalDate;

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
    public String showIndex(
            @RequestParam(required = false) String date,
            Model model) {

        if (date == null || date.isEmpty()) {

            date = java.time.LocalDate.now().toString();
        }

        AvailablePackagesResponse data =
                apiController.getAvailablePackages(date);

        model.addAttribute("venues", data.getVenues());

        model.addAttribute("cateringPackages",
                data.getCateringPackages());

        model.addAttribute("status",
                data.getStatus());

        model.addAttribute("selectedDate",
                date);

        return "index";
    }

    //this is a method to process the order
    //to satisfy ACID requirements
    //to ensure that if one part of the order fails the other is NOT finalized
    @PostMapping("/broker/confirm-order")
    public String confirmOrder(OrderRequest request, Model model) {
        try {
            // Use the logic already defined in the apiController
            boolean vRes =
                    apiController.reserveVenue(
                            request.getSelectedVenue(),
                            request.getDate());
            boolean cRes =
                    apiController.reserveCatering(
                            request.getSelectedCatering(),
                            request.getDate());

            if (vRes && cRes) {
                // this is where Lotte will handle the data addition to the database

                model.addAttribute("message", "Order Placed Successfully!");
                return "order-success";
            } else {
                throw new Exception("One of the suppliers declined.");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Transaction Failed: " + e.getMessage());
            return "order-failed";
        }
    }


    @PostMapping("/broker/review-order")
    public String reviewOrder(OrderRequest request, Model model) {
        // Reuse the logic Lotte already wrote to get the fresh data
        AvailablePackagesResponse data = apiController.getAvailablePackages(request.getDate());

        // Find the Venue object that matches the ID the user picked
        VenueHall selectedVenue = data.getVenues().stream()
                .filter(v -> v.getId().equals(request.getSelectedVenue()))
                .findFirst()
                .orElse(null);

        // Find the Catering object
        CateringPackage selectedCatering = data.getCateringPackages().stream()
                .filter(c -> c.getId().equals(request.getSelectedCatering()))
                .findFirst()
                .orElse(null);

        // Pass the WHOLE objects to the page, not just IDs
        model.addAttribute("venue", selectedVenue);
        model.addAttribute("catering", selectedCatering);
        model.addAttribute("orderRequest", request); // Keeps address/card info

        // Calculate total on the fly
        double total = selectedVenue.getPricePerDay() + selectedCatering.getPricePerPerson();
        model.addAttribute("totalPrice", total);

        return "review";
    }

}
