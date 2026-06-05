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
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Collections;
import java.util.List;
import java.time.LocalDate;
import java.util.UUID;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class BrokerViewController {

    private static final Logger log = LoggerFactory.getLogger(BrokerViewController.class);

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

    @Autowired
    private final BrokerController apiController;

    @Autowired
    private OrderRepository orderRepository;

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
    //implements SAGA pattern: compensating transactions on failure
    //to ensure consistency: if one part fails, the other is rolled back
    @PostMapping("/broker/confirm-order")
    @Transactional
    public String confirmOrder(@RequestParam("orderId") String orderId,
                               @RequestParam("selectedVenue") String selectedVenue,
                               @RequestParam("selectedCatering") String selectedCatering,
                               @RequestParam("date") String date,
                               Model model) {
        log.info("=== CONFIRM ORDER START ===");

        //log.warn("CRITICAL: Broker stort nu volledig ter aarde!");
        //System.exit(0);

        log.info("Attempting to confirm order with ID: {}", orderId);
        log.info("All orders currently in database: {}", orderRepository.count());
        
        Optional<Order> existingOrderOpt = orderRepository.findById(orderId);
        
        if (!existingOrderOpt.isPresent()) {
            log.error("Order not found in database! Order ID: {}", orderId);
            log.error("Total orders in DB: {}", orderRepository.count());
            List<Order> allOrders = orderRepository.findAll();
            for (Order o : allOrders) {
                log.error("  - Found Order: ID={}, Status={}, Venue={}", o.getId(), o.getStatus(), o.getVenueId());
            }
            model.addAttribute("error", "Order not found in database.");
            return "order-failed";
        }
        
        log.info("✓ Order found! Status: {}", existingOrderOpt.get().getStatus());
        
        Order order = existingOrderOpt.get();
        boolean venueConfirmed = false;
        boolean cateringConfirmed = false;
        
        try {
            // PHASE 1: Confirm Venue
            venueConfirmed = apiController.confirmVenue(selectedVenue, date);
            
            // PHASE 2: Confirm Catering
            if (venueConfirmed) {
                cateringConfirmed = apiController.confirmCatering(selectedCatering, date);
            }

            if (venueConfirmed && cateringConfirmed) {
                order.setStatus("CONFIRMED");
                saveOrderToDatabase(order);
                model.addAttribute("message", "Order Confirmed Successfully!");
                return "order-success";
            } 

            log.warn("One of the suppliers failed to confirm. Venue confirmed: {}, Catering confirmed: {}", venueConfirmed, cateringConfirmed);
            
            StringBuilder pending = new StringBuilder();
            if (!venueConfirmed) pending.append("venue");
            if (!cateringConfirmed) {
                if (pending.length() > 0) pending.append(",");
                pending.append("catering");
            }

            order.setStatus("PENDING");
            order.setPendingCompensations(pending.toString());
            order.setRetryCount(0);
            order.setLastRetryTime(new Date());
            saveOrderToDatabase(order);

            model.addAttribute("orderId", orderId);
            model.addAttribute("message", "Order is pending. We will automatically retry for 15minutes. Please wait...");
            return "order-processing";

            } catch (Exception e) {
                log.error("Network error occurred while processing order. Order is put in retry queue.", e);
                
                order.setStatus("PENDING");
                order.setPendingCompensations("venue,catering");
                order.setRetryCount(0);
                order.setLastRetryTime(new Date());
                saveOrderToDatabase(order);
                
                model.addAttribute("orderId", orderId);
                return "order-processing";
            }

    }

    @org.springframework.transaction.annotation.Transactional
    public void saveOrderToDatabase(Order order) {
        log.info("Saving order to database: ID={}, Status={}, Venue={}, Catering={}", 
                 order.getId(), order.getStatus(), order.getVenueId(), order.getCateringId());
        try {
            // Fallback: if createdAt is still NULL, set it now
            if (order.getCreatedAt() == null) {
                log.warn("CreatedAt was NULL for order {}. Setting it now.", order.getId());
                order.setCreatedAt(new java.util.Date());
            }
            orderRepository.save(order);
            orderRepository.flush();
            log.info("✓ Order saved and flushed successfully! CreatedAt timestamp: {}", 
                     order.getCreatedAt() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(order.getCreatedAt()) : "NOT SET");
        } catch (Exception e) {
            log.error("Error saving order to database: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/broker/review-order")
    @Transactional
    public String reviewOrder(OrderRequest request, Model model) {
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

        if (selectedVenue == null || selectedCatering == null) {
            model.addAttribute("error", "Selected options are no longer available. Please try again.");
            return "order-failed";
        }

        double total = selectedVenue.getPricePerDay() + selectedCatering.getPricePerPerson()*selectedVenue.getCapacity();
        String orderId = UUID.randomUUID().toString(); // Generate a unique order ID for this review session

        // SAGA pattern: two-phase reservation with compensating transactions
        boolean venueReserved = false;
        boolean cateringReserved = false;

        try {
            // PHASE 1: Reserve Venue
            log.info("Phase 1: Attempting to reserve venue {} for date {}", request.getSelectedVenue(), request.getDate());
            venueReserved = apiController.reserveVenue(request.getSelectedVenue(), request.getDate());
            
            if (!venueReserved) {
                log.warn("Venue reservation failed for venue {}", request.getSelectedVenue());
                throw new Exception("Venue reservation failed.");
            }
            log.info("Venue reservation successful!");

            // PHASE 2: Reserve Catering
            log.info("Phase 2: Attempting to reserve catering {} for date {}", request.getSelectedCatering(), request.getDate());
            cateringReserved = apiController.reserveCatering(request.getSelectedCatering(), request.getDate());
            
            if (!cateringReserved) {
                log.warn("Catering reservation failed for catering {}", request.getSelectedCatering());
                // COMPENSATING TRANSACTION: Release venue reservation
                boolean venueRelease = apiController.releaseVenue(request.getSelectedVenue(), request.getDate());
                
                if (!venueRelease) {
                    log.error("CRITICAL: Could not release venue {} after failed catering reservation for date {}", 
                              request.getSelectedVenue(), request.getDate());
                    model.addAttribute("error", "CRITICAL: Catering reservation failed and venue release also failed. Please contact support.");
                } else {
                    model.addAttribute("error", "Catering is no longer available. Your venue reservation has been released. Please try again.");
                }
                return "order-failed";
            }
            log.info("Catering reservation successful!");

            // SUCCESS: Both reservations succeeded
            log.info("Both reservations successful! Creating order with ID: {}", orderId);
            Order reservedOrder = new Order();
            reservedOrder.setId(orderId);
            reservedOrder.setVenueId(request.getSelectedVenue());
            reservedOrder.setCateringId(request.getSelectedCatering());
            reservedOrder.setStatus("RESERVED");
            reservedOrder.setTotalPrice(total);
            reservedOrder.setAddress(request.getAddress());
            reservedOrder.setCardNumber(request.getCardNumber());
            reservedOrder.setCreatedAt(new java.util.Date()); // Manually set createdAt timestamp
            
            java.util.Date orderDate;
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                orderDate = sdf.parse(request.getDate());
                reservedOrder.setDate(orderDate);
            } catch (Exception e) {
                log.warn("Could not parse order date, using current date instead: {}", e.getMessage());
                reservedOrder.setDate(new java.util.Date());
            }

            saveOrderToDatabase(reservedOrder);

            log.info("Order saved to database successfully with ID: {} and status: RESERVED", orderId);
            
            // Verify the order was saved
            Optional<Order> verifyOrder = orderRepository.findById(orderId);
            if (verifyOrder.isPresent()) {
                log.info("VERIFIED: Order found in database after save: ID={}, Status={}", orderId, verifyOrder.get().getStatus());
            } else {
                log.error("ERROR: Order NOT found in database after save! ID={}", orderId);
            }

            // Pass the WHOLE objects to the page, not just IDs
            model.addAttribute("venue", selectedVenue);
            model.addAttribute("catering", selectedCatering);
            model.addAttribute("orderRequest", request); // Keeps address/card info
            model.addAttribute("totalPrice", total);
            model.addAttribute("orderId", orderId); 
            model.addAttribute("date", request.getDate());
            model.addAttribute("address", request.getAddress());
            model.addAttribute("cardNumber", request.getCardNumber());
            
            log.info("Redirecting to review page with orderId: {}", orderId);
            return "review";
            
        } catch (Exception e) {
            // Exception during reservation - attempt to rollback what was reserved
            log.error("Exception during reservation phase for order {}: {}", orderId, e.getMessage(), e);
            
            if (venueReserved) {
                log.info("Attempting to release venue reservation as compensating transaction");
                boolean venueRelease = apiController.releaseVenue(request.getSelectedVenue(), request.getDate());
                if (!venueRelease) {
                    log.error("CRITICAL: Failed to release venue {} after exception during reservation", 
                              request.getSelectedVenue());
                    model.addAttribute("error", "CRITICAL ERROR: A reservation error occurred and automatic rollback failed. " +
                                                "Please contact support with Order ID: " + orderId);
                    return "order-failed";
                }
            }
            
            if (cateringReserved) {
                log.info("Attempting to release catering reservation as compensating transaction");
                boolean cateringRelease = apiController.releaseCatering(request.getSelectedCatering(), request.getDate());
                if (!cateringRelease) {
                    log.error("CRITICAL: Failed to release catering {} after exception during reservation", 
                              request.getSelectedCatering());
                    model.addAttribute("error", "CRITICAL ERROR: A reservation error occurred and automatic rollback failed. " +
                                                "Please contact support with Order ID: " + orderId);
                    return "order-failed";
                }
            }
            
            model.addAttribute("error", "Reservation Failed and rolled back: " + e.getMessage());
            return "order-failed";
        }
    }

}
