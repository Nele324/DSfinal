package com.DSfinal.Venue;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VenueService {

    private final VenueRepository repository;

    public VenueService(VenueRepository repository) {
        this.repository = repository;
    }

    public List<VenueHall> getAllVenues() {
        return repository.findAll();
    }

    public List<VenueHall> getAvailableVenues(String date) {

        return repository.findAll()
                .stream()
                .filter(v ->
                        v.getReservations() == null ||
                                !v.getReservations().contains(date))
                .toList();
    }

    public VenueHall getVenueById(String id) {
        return repository.findById(id);
    }

    public VenueHall saveVenue(VenueHall hall) {
        return repository.save(hall);
    }

    public ReserveResponse reserveVenue(ReserveRequest request) {

        VenueHall hall =
                repository.findById(request.getVenueId());

        if (hall == null) {
            return new ReserveResponse(false,
                    "Venue not found");
        }

        if (hall.getReservations() == null) {
            hall.setReservations(new ArrayList<>());
        }

        if (hall.getReservations().contains(request.getDate())) {

            return new ReserveResponse(false,
                    "Venue already booked for this date");
        }

        hall.getReservations().add(request.getDate());

        repository.save(hall);

        return new ReserveResponse(true,
                "Venue reserved successfully");
    }
}