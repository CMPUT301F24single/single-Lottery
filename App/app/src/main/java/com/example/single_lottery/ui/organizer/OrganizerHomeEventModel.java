package com.example.single_lottery.ui.organizer;

public class OrganizerHomeEventModel {
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

    public OrganizerHomeEventModel() {
        // 无参数构造函数，Firestore 数据绑定需要
    }

    // 所有字段的 getter 和 setter
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

    public String getEventTime() {
        return time;
    }

    public void setEventTime(String time) {
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
