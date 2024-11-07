package com.example.single_lottery;

/**
 * Model class representing a lottery event.
 * Stores event details including timing, capacity and registration information.
 * Used for Firestore data mapping.
 *
 * @author [Haorui Gao]
 * @version 1.0
 */
public class EventModel {
    private String name;
    private String organizerDeviceID;
    private int waitingListCount;
    private int lotteryCount;
    private String lotteryTime;
    private String registrationDeadline;
    private String time;
    private String posterUrl;
    private String eventId;
    private String description;

    public EventModel() {
        // No-argument constructor, required for Firestore data binding
    }

    // Getters and setters for all fields
    public String getName() {
        return name;
    }

    public void setEventName(String name) {
        this.name = name;
    }

    public String getOrganizerDeviceID() {
        return organizerDeviceID;
    }

    public void setOrganizerDeviceID(String organizerDeviceID) {
        this.organizerDeviceID = organizerDeviceID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(String registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public String getLotteryTime() {
        return lotteryTime;
    }

    public void setLotteryTime(String lotteryTime) {
        this.lotteryTime = lotteryTime;
    }

    public int getWaitingListCount() {
        return waitingListCount;
    }

    public void setWaitingListCount(int waitingListCount) {
        this.waitingListCount = waitingListCount;
    }

    public int getLotteryCount() {
        return lotteryCount;
    }

    public void setLotteryCount(int lotteryCount) {
        this.lotteryCount = lotteryCount;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
