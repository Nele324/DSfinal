package com.DSfinal.Catering;


import org.springframework.data.annotation.Id;


public class CateringOption {

    @Id
    private String id;

    private String name;
    private int maxGuests;
    private double pricePerPerson;
    private CateringStatus status;

    public CateringOption() {
    }

    public CateringOption(String id, String name, int maxGuests,
                           double pricePerPerson, CateringStatus status) {
        this.id = id;
        this.name = name;
        this.maxGuests = maxGuests;
        this.pricePerPerson = pricePerPerson;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxGuests() {
        return maxGuests;
    }

    public void setMaxGuests(int maxGuests) {
        this.maxGuests = maxGuests;
    }

    public double getPricePerPerson() {
        return pricePerPerson;
    }

    public void setPricePerPerson(double pricePerPerson) {
        this.pricePerPerson = pricePerPerson;
    }

    public CateringStatus getStatus() {
        return status;
    }

    public void setStatus(CateringStatus status) {
        this.status = status;
    }
}