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

    @GetMapping
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Catering service is running");
    }

    @GetMapping("/options")
    public ResponseEntity<List<CateringOption>> getAllOptions() {
        return ResponseEntity.ok(cateringService.getAllOptions());
    }

    @GetMapping("/options/{id}")
    public ResponseEntity<?> getOptionById(@PathVariable String id) {

        CateringOption option =
                cateringService.getOptionById(id);

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
                cateringService.reserveCatering(request);

        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<ReserveResponse> cancelReservation(
            @PathVariable String id) {

        ReserveResponse response =
                cateringService.cancelReservation(id);

        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm/{id}")
    public ResponseEntity<ReserveResponse> confirmReservation(
            @PathVariable String id) {

        ReserveResponse response =
                cateringService.confirmReservation(id);

        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }
}