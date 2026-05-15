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

    public List<CateringOption> getAvailablePackages(String date) {

        return repository.findAll()
                .stream()
                .filter(c -> {

                    boolean pending =
                            c.getPendingReservations() != null &&
                                    c.getPendingReservations().contains(date);

                    boolean confirmed =
                            c.getConfirmedReservations() != null &&
                                    c.getConfirmedReservations().contains(date);

                    return !pending && !confirmed;
                })
                .toList();
    }

    public CateringOption getCateringById(String id) {
        return repository.findById(id);
    }

    public CateringOption saveCatering(CateringOption catering) {
        return repository.save(catering);
    }

    public ReserveResponse reservePackage(ReserveRequest request) {

        CateringOption catering =
                repository.findById(request.getCateringId());

        if (catering == null) {
            return new ReserveResponse(false, "Catering not found");
        }

        if (catering.getPendingReservations() == null) {
            catering.setPendingReservations(new ArrayList<>());
        }

        if (catering.getConfirmedReservations() == null) {
            catering.setConfirmedReservations(new ArrayList<>());
        }

        boolean alreadyPending =
                catering.getPendingReservations().contains(request.getDate());

        boolean alreadyConfirmed =
                catering.getConfirmedReservations().contains(request.getDate());

        if (alreadyPending || alreadyConfirmed) {
            return new ReserveResponse(false, "Already reserved");
        }

        catering.getPendingReservations().add(request.getDate());
        repository.save(catering);

        return new ReserveResponse(true, "Catering temporarily reserved");
    }

    public ReserveResponse confirmReservation(String cateringId, String date) {

        CateringOption catering = repository.findById(cateringId);

        if (catering == null) {
            return new ReserveResponse(false, "Catering not found");
        }

        if (catering.getPendingReservations() == null) {
            catering.setPendingReservations(new ArrayList<>());
        }

        if (catering.getConfirmedReservations() == null) {
            catering.setConfirmedReservations(new ArrayList<>());
        }

        catering.getPendingReservations().remove(date);
        catering.getConfirmedReservations().add(date);

        repository.save(catering);

        return new ReserveResponse(true, "Catering confirmed");
    }

    public ReserveResponse cancelReservation(String cateringId, String date) {

        CateringOption catering = repository.findById(cateringId);

        if (catering == null) {
            return new ReserveResponse(false, "Catering not found");
        }

        if (catering.getPendingReservations() != null) {
            catering.getPendingReservations().remove(date);
        }

        repository.save(catering);

        return new ReserveResponse(true, "Catering cancelled");
    }
}