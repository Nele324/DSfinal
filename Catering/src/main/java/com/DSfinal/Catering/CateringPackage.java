//a simple data model of what catering package looks like

package com.DSfinal.Catering;

public class CateringPackage {
    private String id;
    private String name;
    private int maxGuests;
    private double pricePerPerson;
    private boolean available;

    public CateringPackage(String id, String name, int maxGuests, double pricePerPerson, boolean available) {
        this.id = id;
        this.name = name;
        this.maxGuests = maxGuests;
        this.pricePerPerson = pricePerPerson;
        this.available = available;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getMaxGuests() { return maxGuests; }
    public double getPricePerPerson() { return pricePerPerson; }
    public boolean isAvailable() { return available; }

    //setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPricePerPerson(double pricePerPerson) { this.pricePerPerson = pricePerPerson; }
    public void setAvailable(boolean available) { this.available = available; }
}
