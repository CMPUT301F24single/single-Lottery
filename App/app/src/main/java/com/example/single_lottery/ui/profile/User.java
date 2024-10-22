package com.example.single_lottery.ui.profile;

public class User {
    public String name;
    public String email;
    public String phone;
    public String profileImageUrl;

    User(String name, String email, String phone, String profileImageUrl) {
        this.name = name;
        this.email = email;
        this.phone = phone;
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

    String getProfileImageUrl() {
        return profileImageUrl;
    }

}
