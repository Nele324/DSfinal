package com.DSfinal.broker;

import java.util.List;

public class CateringPackage {
    private String id;
    private String name;
    private int maxGuests;
    private double pricePerPerson;
    private List<String> reservations;

    // Getters en Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getMaxGuests() { return maxGuests; }
    public double getPricePerPerson() { return pricePerPerson; }
    public List<String> getReservations() { return reservations; }
    public boolean isAvailable(String date) {

        return reservations == null ||
                !reservations.contains(date);
    }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setMaxGuests(int maxGuests) { this.maxGuests = maxGuests; }
    public void setPrice(double pricePerPerson) { this.pricePerPerson = pricePerPerson; }
    public void setReservations(List<String> reservations) { this.reservations = reservations; }

    @Override
    public String toString() {
        return "CateringPackage{name='" + name + "', price=" + pricePerPerson + "',maxGuests='"+ maxGuests+ "}";
    }
}
