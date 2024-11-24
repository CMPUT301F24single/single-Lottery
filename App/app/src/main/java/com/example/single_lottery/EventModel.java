package com.example.single_lottery;

import java.io.Serializable;

/**
 * Model class representing a lottery event.
 * Stores event details including timing, capacity and registration information.
 * Used for Firestore data mapping.
 *
 * @author [Haorui Gao]
 * @version 1.0
 */
public class EventModel implements Serializable {
    private String name;
    private String organizerDeviceID;
    private String userDeviceID;
    private int waitingListCount;
    private int lotteryCount;
    private String lotteryTime;
    private String registrationDeadline;
    private String time;
    private String posterUrl;
    private String eventId;
    private String description;
    private String profileImageUrl;
    private String email;
    private String phone;
    private String info;

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

    public String getUserDeviceID() {
        return userDeviceID;
    }

    public void setUserDeviceID(String userDeviceID) {
        this.userDeviceID = userDeviceID;
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

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

}

