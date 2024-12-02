package com.example.single_lottery.ui.notification;

public class Notification {
    private String title;
    private String message;
    private String userId;

    public Notification(String title, String message, String userId) {
        this.title = title;
        this.message = message;
        this.userId = userId; // Assign the userId correctly
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
