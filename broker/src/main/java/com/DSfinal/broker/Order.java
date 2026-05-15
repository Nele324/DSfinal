package com.DSfinal.broker;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;


//!!! IF more columns are added to the order table
// REMEMBER to update in manager-view-orders.html -->
@Entity
@Table(name = "Orders")
public class Order {
    @Id
    @Column(name = "Id")
    private String id;

    @Column(name = "VenueId")
    private String venueId;

    @Column(name = "CateringId")
    private String cateringId;

    // Constructors
    public Order() {}
    public Order(String id, String venueId, String cateringId) {
        this.id = id;
        this.venueId = venueId;
        this.cateringId = cateringId;
    }

    // Getters & Setters
    public String getId() { return id; }
    public String getVenueId() { return venueId; }
    public String getCateringId() { return cateringId; }
}