package com.example.single_lottery.ui.organizer;
public class Facility {
    public String name;
    public String location;
    public String profileImageUrl;
    public String facility;
    public Facility(String name, String location, String profileImageUrl) {
        this.name = name;
        this.location = location;
        this.profileImageUrl = profileImageUrl;
    }
    public String getName() {
        return name;
    }
    public String getLocation() {
        return location;
    }
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
