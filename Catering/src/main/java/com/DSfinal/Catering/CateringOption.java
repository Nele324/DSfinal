package com.DSfinal.Catering;


import org.springframework.data.annotation.Id;

import java.util.List;


public class CateringOption {

    @Id
    private String id;

    private String name;
    private int maxGuests;
    private double pricePerPerson;
    private List<String> reservations;

    public CateringOption() {
    }

    public CateringOption(String id, String name, int maxGuests, double pricePerPerson, List<String> reservations) {
        this.id = id;
        this.name = name;
        this.maxGuests = maxGuests;
        this.pricePerPerson = pricePerPerson;
        this.reservations = reservations;
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

    public List<String> getReservations() { return reservations; }

    public void setReservations(List<String> reservations) { this.reservations = reservations; }
}