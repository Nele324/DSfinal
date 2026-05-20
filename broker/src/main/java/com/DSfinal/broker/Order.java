package com.DSfinal.broker;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.util.Date;


//!!! IF more columns are added to the order table
// REMEMBER to update in manager-view-orders.html -->
@Entity
@Table(name = "Orders")
public class Order {
    @Id
    @Column(name = "orderId")
    private String id;

    @Column(name = "venueId")
    private String venueId;

    @Column(name = "cateringId")
    private String cateringId;

    @Column(name = "status")
    private String status;

    @Column(name = "orderDate")
    private Date date;

    @Column(name = "address")
    private String address;

    @Column(name = "paymentCard")
    private String cardNumber;

    @Column(name = "totalPrice")
    private double totalPrice;

    // Constructors
    public Order() {}
    public Order(String id, String venueId, String cateringId, Date date) {
        this.id = id;
        this.venueId = venueId;
        this.cateringId = cateringId;
        this.status = "PENDING";
        this.date = date;
        this.totalPrice = 0.0;
    }

    // Getters & Setters
    public String getId() { return id; }
    public String getVenueId() { return venueId; }
    public String getCateringId() { return cateringId; }
    public String getStatus() { return status; }
    public Date getDate() { return date; }
    public double getTotalPrice() { return totalPrice; }
    public String getAddress() { return address; }
    public String getCardNumber() { return cardNumber; }
    public void setId(String id) { this.id = id; }
    public void setStatus(String status) { this.status = status; }
    public void setCateringId(String cateringId) { this.cateringId = cateringId; }
    public void setVenueId(String venueId) { this.venueId = venueId; }
    public void setDate(Date date) { this.date = date; }
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
    public void setAddress(String address) { this.address = address; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
}