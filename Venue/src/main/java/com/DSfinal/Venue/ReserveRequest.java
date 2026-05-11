package com.DSfinal.Venue;

public class ReserveRequest {

    private String venueId;
    private int guests;

    public String getVenueId() {
        return venueId;
    }

    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }

    public int getGuests() {
        return guests;
    }

    public void setGuests(int guests) {
        this.guests = guests;
    }
}