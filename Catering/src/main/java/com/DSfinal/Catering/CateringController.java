package com.DSfinal.Catering;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catering")
public class CateringController {

    private final CateringService cateringService;

    public CateringController(CateringService cateringService) {
        this.cateringService = cateringService;
    }

    @GetMapping("/options")
    public List<CateringOption> getOptions() {
        return cateringService.getAllCateringOptions();
    }
}