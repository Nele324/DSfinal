package com.DSfinal.Venue;

public class ReserveRequest {

    private String venueId;
    private int guests;
    private String date;

    public String getVenueId() { return venueId; }

    public void setVenueId(String venueId) { this.venueId = venueId; }

    public int getGuests() { return guests; }

    public void setGuests(int guests) { this.guests = guests; }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }
}