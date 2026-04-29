//a simple data model of what a venue hall looks like
package com.DSfinal.Venue;

public class VenueHall {

    private String id;
    private String name;
    private int capacity;
    private double pricePerDay;
    private boolean available;

    public VenueHall(String id, String name, int capacity, double pricePerDay, boolean available) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.pricePerDay = pricePerDay;
        this.available = available;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public double getPricePerDay() { return pricePerDay; }
    public boolean isAvailable() { return available; }
}
