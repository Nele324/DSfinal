package com.DSfinal.Venue;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/venue")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Venue service is running");
    }

    @GetMapping("/halls")
    public ResponseEntity<List<VenueHall>> getAllHalls(
            @RequestParam(required = false) String date) {

        if (date == null) {
            return ResponseEntity.ok(
                    venueService.getAllVenues());
        }

        return ResponseEntity.ok(
                venueService.getAvailableVenues(date));
    }

    @GetMapping("/halls/{id}")
    public ResponseEntity<?> getHallById(
            @PathVariable String id) {

        VenueHall hall =
                venueService.getVenueById(id);

        if (hall == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(hall);
    }

    @PostMapping("/reserve")
    public ResponseEntity<ReserveResponse> reserve(
            @RequestBody ReserveRequest request) {

        ReserveResponse response =
                venueService.reserveVenue(request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest()
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<String> cancelReservation(
            @RequestParam String venueId,
            @RequestParam String date) {

        VenueHall hall = venueService.getVenueById(venueId);

        if (hall == null) {
            return ResponseEntity.badRequest()
                    .body("Venue not found");
        }

        if (hall.getReservations() != null) {
            hall.getReservations().remove(date);
        }

        venueService.saveVenue(hall);

        return ResponseEntity.ok("Reservation cancelled");
    }
}