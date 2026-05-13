package com.DSfinal.broker;

import java.util.List;

public class VenueHall {
    private String id;
    private String name;
    private int capacity;
    private double pricePerDay;
    private List<String> reservations;

    // Getters en Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public double getPricePerDay() { return pricePerDay; }
    public List<String> getReservations() { return reservations; }
    public boolean isAvailable(String date) {

        return reservations == null ||
                !reservations.contains(date);
    }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setPricePerDay(double pricePerDay) { this.pricePerDay = pricePerDay; }
    public void setReservations(List<String> reservations) { this.reservations = reservations; }

    @Override
    public String toString() {
        return "VenueHall{name='" + name + "', price=" + pricePerDay + "',capacity='" + capacity + "}";
    }
}
