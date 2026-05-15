package com.DSfinal.Catering;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.List;

@RestController
@RequestMapping("/catering")
public class CateringController {

    private final CateringService cateringService;

    public CateringController(CateringService cateringService) {
        this.cateringService = cateringService;
    }

    @GetMapping("/options")
    public ResponseEntity<List<CateringOption>> getAllOptions(
            @RequestParam(required = false) String date) {

        if (date == null) {
            return ResponseEntity.ok(cateringService.getAllPackages());
        }

        return ResponseEntity.ok(cateringService.getAvailablePackages(date));
    }

    @GetMapping("/options/{id}")
    public ResponseEntity<?> getOptionById(@PathVariable String id) {

        CateringOption option = cateringService.getCateringById(id);

        if (option == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Catering option not found");
        }

        return ResponseEntity.ok(option);
    }

    @PostMapping("/reserve")
    public ResponseEntity<ReserveResponse> reserve(
            @RequestBody ReserveRequest request) {

        ReserveResponse response =
                cateringService.reservePackage(request);

        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel")
    public ResponseEntity<ReserveResponse> cancel(
            @RequestParam String cateringId,
            @RequestParam String date) {

        ReserveResponse response =
                cateringService.cancelReservation(cateringId, date);

        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<ReserveResponse> confirm(
            @RequestParam String cateringId,
            @RequestParam String date) {

        ReserveResponse response =
                cateringService.confirmReservation(cateringId, date);

        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }
}