package com.DSfinal.Venue;

import org.springframework.stereotype.Service;

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

        if (hall.getStatus() != VenueStatus.AVAILABLE) {
            return new ReserveResponse(false,
                    "Venue not available");
        }

        if (request.getGuests() > hall.getCapacity()) {
            return new ReserveResponse(false,
                    "Too many guests");
        }

        hall.setStatus(VenueStatus.RESERVED);

        repository.save(hall);

        return new ReserveResponse(true,
                "Venue reserved successfully");
    }

    public ReserveResponse cancelReservation(String id) {

        VenueHall hall = repository.findById(id);

        if (hall == null) {
            return new ReserveResponse(false,
                    "Venue not found");
        }

        hall.setStatus(VenueStatus.AVAILABLE);

        repository.save(hall);

        return new ReserveResponse(true,
                "Reservation cancelled");
    }

    public ReserveResponse confirmReservation(String id) {

        VenueHall hall = repository.findById(id);

        if (hall == null) {

            return new ReserveResponse(
                    false,
                    "Venue not found");
        }

        if (hall.getStatus() != VenueStatus.RESERVED) {

            return new ReserveResponse(
                    false,
                    "Venue must first be RESERVED");
        }

        hall.setStatus(VenueStatus.CONFIRMED);

        repository.save(hall);

        return new ReserveResponse(
                true,
                "Reservation confirmed");
    }
}