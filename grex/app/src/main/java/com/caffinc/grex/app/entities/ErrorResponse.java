package com.caffinc.grex.app.entities;

/**
 * Returned when there's an error in the API call
 *
 * @author Sriram
 */
public class ErrorResponse {
    private int code;
    private String reason;

    public ErrorResponse(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
