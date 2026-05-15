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

    public List<VenueHall> getAvailableVenues(
            String date) {

        return repository.findAll()
                .stream()
                .filter(v -> {

                    boolean pending =
                            v.getPendingReservations() != null &&
                                    v.getPendingReservations().contains(date);

                    boolean confirmed =
                            v.getConfirmedReservations() != null &&
                                    v.getConfirmedReservations().contains(date);

                    return !pending && !confirmed;
                })
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

            return new ReserveResponse(
                    false,
                    "Venue not found"
            );
        }

        if (hall.getPendingReservations() == null) {
            hall.setPendingReservations(new ArrayList<>());
        }

        if (hall.getConfirmedReservations() == null) {
            hall.setConfirmedReservations(new ArrayList<>());
        }

        boolean alreadyPending =
                hall.getPendingReservations()
                        .contains(request.getDate());

        boolean alreadyConfirmed =
                hall.getConfirmedReservations()
                        .contains(request.getDate());

        if (alreadyPending || alreadyConfirmed) {

            return new ReserveResponse(
                    false,
                    "Venue already reserved"
            );
        }

        hall.getPendingReservations()
                .add(request.getDate());

        repository.save(hall);

        return new ReserveResponse(
                true,
                "Venue temporarily reserved"
        );
    }

    public ReserveResponse confirmReservation(
            String venueId,
            String date) {

        VenueHall hall = repository.findById(venueId);

        if (hall == null) {

            return new ReserveResponse(
                    false,
                    "Venue not found"
            );
        }

        if (hall.getPendingReservations() != null) {

            hall.getPendingReservations().remove(date);
        }

        if (hall.getConfirmedReservations() == null) {

            hall.setConfirmedReservations(
                    new ArrayList<>()
            );
        }

        hall.getConfirmedReservations().add(date);

        repository.save(hall);

        return new ReserveResponse(
                true,
                "Venue confirmed"
        );
    }

    public ReserveResponse cancelReservation(
            String venueId,
            String date) {

        VenueHall hall = repository.findById(venueId);

        if (hall == null) {

            return new ReserveResponse(
                    false,
                    "Venue not found"
            );
        }

        if (hall.getPendingReservations() != null) {

            hall.getPendingReservations().remove(date);
        }

        repository.save(hall);

        return new ReserveResponse(
                true,
                "Venue reservation cancelled"
        );
    }
}