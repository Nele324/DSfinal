package com.DSfinal.broker;

//this is a Data Object
public class OrderRequest {
    private String selectedVenue;
    private String selectedCatering;
    private String address;
    private String cardNumber;

    public String getSelectedVenue() {
        return selectedVenue;
    }

    public void setSelectedVenue(String selectedVenue) {
        this.selectedVenue = selectedVenue;
    }

    public String getSelectedCatering() {
        return selectedCatering;
    }

    public void setSelectedCatering(String selectedCatering) {
        this.selectedCatering = selectedCatering;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
}
