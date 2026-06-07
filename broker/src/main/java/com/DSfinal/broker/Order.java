package com.DSfinal.broker;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import org.hibernate.annotations.CreationTimestamp;


//!!! IF more columns are added to the order table
// REMEMBER to update in manager-view-orders.html -->
// Current columns: orderId, venueId, cateringId, status, orderDate, address, paymentCard, totalPrice, createdAt
@Entity
@Table(name = "Orders")
public class Order {
    @Id
    @Column(name = "order_id")
    private String id;

    @Column(name = "venue_id")
    private String venueId;

    @Column(name = "catering_id")
    private String cateringId;

    @Column(name = "status")
    private String status;

    @Column(name = "order_date")
    private Date date;

    @Column(name = "address")
    private String address;

    @Column(name = "payment_card")
    private String cardNumber;

    @Column(name = "total_price")
    private double totalPrice;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_retry_time")
    private Date lastRetryTime;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "pending_compensations", length = 500)
    private String pendingCompensations; // CSV list: "venue,catering" indicating which still need rollback

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
    public Date getCreatedAt() { return createdAt; }
    public Date getLastRetryTime() { return lastRetryTime; }
    public Integer getRetryCount() { return retryCount; }
    public String getPendingCompensations() { return pendingCompensations; }
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
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public void setLastRetryTime(Date lastRetryTime) { this.lastRetryTime = lastRetryTime; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public void setPendingCompensations(String pendingCompensations) { this.pendingCompensations = pendingCompensations; }
}