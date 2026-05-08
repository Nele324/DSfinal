package com.DSfinal.Catering;

public class CateringOption {

    private String id;
    private String name;
    private int maxGuests;
    private double pricePerPerson;
    private boolean available;

    public CateringOption() {}

    public CateringOption(String id, String name, int maxGuests, double pricePerPerson, boolean available) {
        this.id = id;
        this.name = name;
        this.maxGuests = maxGuests;
        this.pricePerPerson = pricePerPerson;
        this.available = available;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getMaxGuests() { return maxGuests; }
    public void setMaxGuests(int maxGuests) { this.maxGuests = maxGuests; }

    public double getPricePerPerson() { return pricePerPerson; }
    public void setPricePerPerson(double pricePerPerson) { this.pricePerPerson = pricePerPerson; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}