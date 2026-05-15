package com.DSfinal.Venue;

import java.util.List;

public class VenueHall {

    private String id;
    private String name;
    private int capacity;
    private double pricePerDay;
    private List<String> pendingReservations;
    private List<String> confirmedReservations;

    public VenueHall() {}

    public VenueHall(String id, String name, int capacity, double pricePerDay, List<String> pendingReservations, List<String> confirmedReservations) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.pricePerDay = pricePerDay;
        this.pendingReservations = pendingReservations;
        this.confirmedReservations = confirmedReservations;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public double getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(double pricePerDay) { this.pricePerDay = pricePerDay; }

    public List<String> getPendingReservations() { return pendingReservations; }
    public void setPendingReservations(List<String> pendingReservations) { this.pendingReservations = pendingReservations; }

    public List<String> getConfirmedReservations() { return confirmedReservations; }
    public void setConfirmedReservations(List<String> confirmedReservations) { this.confirmedReservations = confirmedReservations; }
}