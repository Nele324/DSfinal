//controller to handle incoming requests and
//decides what should happen next
package com.DSfinal.Catering;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/catering")
public class CateringController {

    @GetMapping("/packages")
    public List<CateringPackage> getPackages() {
        return List.of(
                new CateringPackage("C1", "Basic Buffet", 50, 15.0, true),
                new CateringPackage("C2", "Premium Dinner", 100, 45.0, true),
                new CateringPackage("C3", "Cocktail Reception", 150, 30.0, false)
        );
    }
}
