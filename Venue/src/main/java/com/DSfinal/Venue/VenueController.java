package com.DSfinal.Venue;

import org.springframework.http.HttpStatus;
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

        return ResponseEntity.ok(
                "Venue service is running"
        );
    }

    @GetMapping("/halls")
    public ResponseEntity<List<VenueHall>>
    getAllHalls(
            @RequestParam(required = false)
            String date) {

        if (date == null) {

            return ResponseEntity.ok(
                    venueService.getAllVenues()
            );
        }

        return ResponseEntity.ok(
                venueService
                        .getAvailableVenues(date)
        );
    }

    @GetMapping("/halls/{id}")
    public ResponseEntity<?> getHallById(
            @PathVariable String id) {

        VenueHall hall =
                venueService.getVenueById(id);

        if (hall == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Venue not found");
        }

        return ResponseEntity.ok(hall);
    }

    @PostMapping("/reserve")
    public ResponseEntity<ReserveResponse>
    reserve(
            @RequestBody ReserveRequest request) {

        ReserveResponse response =
                venueService
                        .reserveVenue(request);

        if (!response.isSuccess()) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel")
    public ResponseEntity<ReserveResponse> cancel(
            @RequestParam String venueId,
            @RequestParam String date) {

        ReserveResponse response =
                venueService.cancelReservation(
                        venueId,
                        date
                );

        if (!response.isSuccess()) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<ReserveResponse> confirm(
            @RequestParam String venueId,
            @RequestParam String date) {

        ReserveResponse response =
                venueService.confirmReservation(
                        venueId,
                        date
                );

        if (!response.isSuccess()) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }
}