package com.example.single_lottery.ui.organizer;

/**
 * Model class for storing organizer profile information in the Single Lottery system.
 *
 * @author [Haorui Gao]
 * @version 1.0
 */
public class OrganizerProfile {
    public String name;
    public String email;
    public String phone;
    public String info;
    public String profileImageUrl;

    /**
     * Creates new organizer profile with specified details.
     *
     * @param name Organizer name
     * @param email Email address
     * @param phone Phone number
     * @param info Additional info
     * @param profileImageUrl Profile image URL
     */
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