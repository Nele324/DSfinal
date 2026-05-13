package com.DSfinal.Venue;

import java.util.List;

public class VenueHall {

    private String id;
    private String name;
    private int capacity;
    private double pricePerDay;
    private List<String> reservations;

    public VenueHall() {}

    public VenueHall(String id, String name, int capacity, double pricePerDay, List<String> reservations) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.pricePerDay = pricePerDay;
        this.reservations = reservations;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public double getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(double pricePerDay) { this.pricePerDay = pricePerDay; }

    public List<String> getReservations() { return reservations; }
    public void setReservations(List<String> reservations) { this.reservations = reservations; }
}