package com.example.single_lottery.ui.notification;
/**
 * Model class representing a notification message.
 * Stores notification title and message content.
 *
 * @author Haorui Gao
 * @version 1.0
 */
public class Notification {
    private String title;
    private String message;
    /**
     * Creates a new notification with title and message.
     *
     * @param title The notification title
     * @param message The notification message content
     */
    public Notification(String title, String message) {
        this.title = title;
        this.message = message;
    }
    /**
     * Gets the notification title.
     *
     * @return The notification title
     */
    public String getTitle() {
        return title;
    }
    /**
     * Sets the notification title.
     *
     * @param title The new notification title
     */
    public void setTitle(String title) {
        this.title = title;
    }
    /**
     * Gets the notification message.
     *
     * @return The notification message content
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the notification message.
     *
     * @param message The new notification message content
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
