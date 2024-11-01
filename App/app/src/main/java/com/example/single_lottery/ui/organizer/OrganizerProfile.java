package com.example.single_lottery.ui.organizer;

public class OrganizerProfile {
    public String name;
    public String email;
    public String phone;
    public String info;
    public String profileImageUrl;

    OrganizerProfile(String name, String email, String phone, String info, String profileImageUrl) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.info = info;

        this.profileImageUrl = profileImageUrl;
    }

    String getName() {
        return name;
    }

    String getEmail() {
        return email;
    }

    String getPhone() {
        return phone;
    }

    String getInfo() {
        return info;
    }

    String getProfileImageUrl() {
        return profileImageUrl;
    }

}