package com.DSfinal.Venue;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/venue")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping("/halls")
    public List<VenueHall> getHalls() {
        return venueService.getAllVenues();
    }
}