//
package com.DSfinal.broker;

import java.util.List;
import java.util.Map;

public class AvailablePackagesResponse {

    private List<Map<String, Object>> venues;
    private List<Map<String, Object>> cateringPackages;
    private String status;

    public AvailablePackagesResponse(List<Map<String, Object>> venues,
                                     List<Map<String, Object>> cateringPackages,
                                     String status) {
        this.venues = venues;
        this.cateringPackages = cateringPackages;
        this.status = status;
    }

    public List<Map<String, Object>> getVenues() {
        return venues;
    }

    public List<Map<String, Object>> getCateringPackages() {
        return cateringPackages;
    }

    public String getStatus() {
        return status;
    }
}
