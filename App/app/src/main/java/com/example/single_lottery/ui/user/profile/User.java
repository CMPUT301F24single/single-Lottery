package com.example.single_lottery.ui.user.profile;

/**
 * Model class representing a user profile.
 * Stores basic user information including name, contact details and profile image URL.
 *
 * @author Haorui Gao
 * @version 1.0
 */
public class User {
    public String name;
    public String email;
    public String phone;
    public String profileImageUrl;
    /**
     * Creates a new user with the specified details.
     *
     * @param name User's display name
     * @param email User's email address
     * @param phone User's phone number
     * @param profileImageUrl URL to user's profile image
     */
    public User(String name, String email, String phone, String profileImageUrl) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.profileImageUrl = profileImageUrl;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }


}
