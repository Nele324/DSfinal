package com.DSfinal.Catering;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CateringService {

    private final CateringRepository repository;

    public CateringService(CateringRepository repository) {
        this.repository = repository;
    }

    public List<CateringOption> getAllPackages() {

        return repository.findAll();
    }

    public List<CateringOption> getAvailablePackages(
            String date) {

        return repository.findAll()
                .stream()
                .filter(c ->
                        c.getReservations() == null ||
                                !c.getReservations().contains(date))
                .toList();
    }


    public ReserveResponse reservePackage(
            ReserveRequest request) {

        CateringOption catering =
                repository.findById(request.getCateringId());

        if (catering == null) {

            return new ReserveResponse(
                    false,
                    "Catering not found"
            );
        }

        if (catering.getReservations() == null) {

            catering.setReservations(
                    new ArrayList<>()
            );
        }

        if (catering.getReservations()
                .contains(request.getDate())) {

            return new ReserveResponse(
                    false,
                    "Catering already booked for this date"
            );
        }

        catering.getReservations()
                .add(request.getDate());

        repository.save(catering);

        return new ReserveResponse(
                true,
                "Catering reserved successfully"
        );
    }
}