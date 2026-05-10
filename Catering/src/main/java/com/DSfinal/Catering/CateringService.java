package com.DSfinal.Catering;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CateringService {

    private final CateringRepository repository;

    public CateringService(CateringRepository repository) {
        this.repository = repository;
    }

    public List<CateringOption> getAllOptions() {
        return repository.findAll();
    }

    public CateringOption getOptionById(String id) {
        return repository.findById(id);
    }

    public CateringOption saveOption(CateringOption option) {
        return repository.save(option);
    }

    public ReserveResponse reserveCatering(ReserveRequest request) {

        CateringOption option =
                repository.findById(request.getCateringId());

        if (option == null) {
            return new ReserveResponse(false, "Option not found");
        }

        if (option.getStatus() != CateringStatus.AVAILABLE) {
            return new ReserveResponse(false, "Option not available");
        }

        if (request.getGuests() > option.getMaxGuests()) {
            return new ReserveResponse(false,
                    "Too many guests");
        }

        option.setStatus(CateringStatus.RESERVED);

        repository.save(option);

        return new ReserveResponse(true,
                "Reservation successful");
    }

    public ReserveResponse cancelReservation(String id) {

        CateringOption option = repository.findById(id);

        if (option == null) {
            return new ReserveResponse(false, "Option not found");
        }

        option.setStatus(CateringStatus.AVAILABLE);

        repository.save(option);

        return new ReserveResponse(true,
                "Reservation cancelled");
    }

    public ReserveResponse confirmReservation(String id) {

        CateringOption option =
                repository.findById(id);

        if (option == null) {

            return new ReserveResponse(
                    false,
                    "Option not found");
        }

        if (option.getStatus() != CateringStatus.RESERVED) {

            return new ReserveResponse(
                    false,
                    "Option must first be RESERVED");
        }

        option.setStatus(CateringStatus.CONFIRMED);

        repository.save(option);

        return new ReserveResponse(
                true,
                "Reservation confirmed");
    }
}