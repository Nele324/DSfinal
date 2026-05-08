package com.DSfinal.broker;

public class CombinedOption {
    private String venueId;
    private String cateringId;
    private String venueName;
    private String cateringName;
    private double totalPrice;
    private boolean available;
    private int cateringMaxGuests;
    private int venueMaxGuests;

    public CombinedOption() {}

    public CombinedOption(VenueHall venue, CateringPackage catering) {
        this.venueId = venue.getId();
        this.cateringId = catering.getId();
        this.venueName = venue.getName();
        this.cateringName = catering.getName();
        this.totalPrice = venue.getPricePerDay() + catering.getPricePerPerson();
        this.available = venue.isAvailable() && catering.isAvailable();
        this.cateringMaxGuests = catering.getMaxGuests();
        this.venueMaxGuests= venue.getCapacity();
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

    public int getCateringMaxGuests() { return cateringMaxGuests; }
    public void setCateringMaxGuests(int cateringMaxGuests) {this.cateringMaxGuests = cateringMaxGuests;}

    public int getVenueMaxGuests() { return venueMaxGuests; }
    public void setVenueMaxGuests(int venueMaxGuests) { this.venueMaxGuests = venueMaxGuests; }
}