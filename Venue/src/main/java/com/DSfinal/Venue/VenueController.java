package com.DSfinal.Venue;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/venue")
public class VenueController {

    @GetMapping("/halls")
    public List<VenueHall> getHalls() {
        return List.of(
                new VenueHall("V1", "Grand Ballroom", 300, 2500.0, true),
                new VenueHall("V2", "Garden Terrace", 80, 1200.0, true),
                new VenueHall("V3", "Rooftop Lounge", 50, 900.0, false)
        );
    }

    @PostMapping("/reserve/{id}")
    public ResponseEntity<Boolean> reserve(@PathVariable String id) {
        // For now, just simulate success.
        // Later, your teammates will update the DB here to set 'available = false'
        System.out.println("Venue " + id + " has been reserved via Broker.");
        return ResponseEntity.ok(true);
    }
}
