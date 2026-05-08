package com.DSfinal.broker;

public class VenueHall {
    private String id;
    private String name;
    private int capacity;
    private double pricePerDay;
    private boolean available;

    // Getters en Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public double getPricePerDay() { return pricePerDay; }
    public boolean isAvailable() { return available; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setPricePerDay(double pricePerDay) { this.pricePerDay = pricePerDay; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return "VenueHall{name='" + name + "', price=" + pricePerDay + "',capacity='" + capacity + ", available=" + available + "}";
    }
}

