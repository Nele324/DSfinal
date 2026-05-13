package com.DSfinal.Catering;

public class ReserveRequest {

    private String cateringId;
    private int guests;
    private String date;

    public String getCateringId() {
        return cateringId;
    }

    public void setCateringId(String cateringId) {
        this.cateringId = cateringId;
    }

    public int getGuests() {
        return guests;
    }

    public void setGuests(int guests) {
        this.guests = guests;
    }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }
}