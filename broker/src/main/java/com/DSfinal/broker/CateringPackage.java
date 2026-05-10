package com.DSfinal.broker;

public class CateringPackage {
    private String id;
    private String name;
    private int maxGuests;
    private double pricePerPerson;
    private String status;

    // Getters en Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getMaxGuests() { return maxGuests; }
    public double getPricePerPerson() { return pricePerPerson; }
    public String getStatus() { return status; }

    public boolean isAvailable() {
        return "AVAILABLE".equals(status);
    }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setMaxGuests(int maxGuests) { this.maxGuests = maxGuests; }
    public void setPrice(double pricePerPerson) { this.pricePerPerson = pricePerPerson; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "CateringPackage{name='" + name + "', price=" + pricePerPerson + "',maxGuests='"+ maxGuests+ ", status=" + status + "}";
    }
}
