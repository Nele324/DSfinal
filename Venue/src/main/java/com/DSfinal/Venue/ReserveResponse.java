package com.DSfinal.Venue;

public class ReserveResponse {

    private boolean success;
    private String message;

    public ReserveResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}