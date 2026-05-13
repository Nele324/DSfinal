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

    public CateringController(
            CateringService cateringService) {

        this.cateringService = cateringService;
    }

    @GetMapping("/options")
    public ResponseEntity<List<CateringOption>>
    getAllOptions(
            @RequestParam(required = false)
            String date) {

        if (date == null) {

            return ResponseEntity.ok(
                    cateringService.getAllPackages()
            );
        }

        return ResponseEntity.ok(
                cateringService
                        .getAvailablePackages(date)
        );
    }

    @PostMapping("/reserve")
    public ResponseEntity<ReserveResponse>
    reserve(
            @RequestBody ReserveRequest request) {

        ReserveResponse response =
                cateringService
                        .reservePackage(request);

        if (!response.isSuccess()) {

            return ResponseEntity.badRequest()
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }
}