package com.DSfinal.broker;

public class VenueHall {
    private String id;
    private String name;
    private int capacity;
    private double pricePerDay;
    private String status;

    // Getters en Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public double getPricePerDay() { return pricePerDay; }
    public String getStatus() { return status; }

    public boolean isAvailable() {
        return "AVAILABLE".equals(status);
    }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setPricePerDay(double pricePerDay) { this.pricePerDay = pricePerDay; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "VenueHall{name='" + name + "', price=" + pricePerDay + "',capacity='" + capacity + ", status=" + status + "}";
    }
}
