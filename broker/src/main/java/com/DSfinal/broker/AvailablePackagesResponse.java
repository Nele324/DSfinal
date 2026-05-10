//
package com.DSfinal.broker;

import java.util.List;
import java.util.Map;

public class AvailablePackagesResponse {
    private List<VenueHall> venues; // Changed from Map to list
    private List<CateringPackage> cateringPackages; // Changed from Map to list
    private String status;

    public AvailablePackagesResponse(List<VenueHall> venues, List<CateringPackage> cateringPackages, String status) {
        this.venues = venues;
        this.cateringPackages = cateringPackages;
        this.status = status;
    }

    public List<VenueHall> getVenues() {
        return venues;
    }

    public List<CateringPackage> getCateringPackages() {
        return cateringPackages;
    }

    public String getStatus() {
        return status;
    }
}