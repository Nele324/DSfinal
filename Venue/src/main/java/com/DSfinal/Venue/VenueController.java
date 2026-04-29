package com.DSfinal.Venue;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
