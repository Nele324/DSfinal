package com.DSfinal.broker;

public class CombinedOption {
    private String venueId;
    private String cateringId;
    private String venueName;
    private String cateringName;
    private double totalPrice;
    private boolean available;

    public CombinedOption() {}

    public CombinedOption(VenueHall venue, CateringPackage catering) {
        this.venueId = venue.getId();
        this.cateringId = catering.getId();
        this.venueName = venue.getName();
        this.cateringName = catering.getName();
        this.totalPrice = venue.getPrice() + catering.getPrice();
        this.available = venue.isAvailable() && catering.isAvailable();
    }

    // Getters en Setters (nodig voor JSON!)
    public String getVenueId() { return venueId; }
    public void setVenueId(String venueId) { this.venueId = venueId; }
    public String getCateringId() { return cateringId; }
    public void setCateringId(String cateringId) { this.cateringId = cateringId; }
    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }
    public String getCateringName() { return cateringName; }
    public void setCateringName(String cateringName) { this.cateringName = cateringName; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}