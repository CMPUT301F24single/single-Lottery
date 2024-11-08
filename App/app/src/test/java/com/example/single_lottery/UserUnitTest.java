package com.example.single_lottery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.example.single_lottery.ui.user.profile.User;

import org.junit.Test;

public class UserUnitTest {
    private final String testName = "testName";
    private final String testEmail = "testEmail";
    private final String testPhone = "123123";

    private User mockUser(){
        User user = new User(testName, testEmail, testPhone, null);
        return user;
    }

    User user1 = new User("Aaron", "test@test.test", "111", null);
    User user2 = new User("Instance", "www", "222", null);

    @Test
    public void testGetName(){
        User user = mockUser();
        assertEquals(testName, user.getName());
        assertEquals("Aaron", user1.getName());
        assertEquals("Instance",user2.getName());
        assertNotEquals("Aaron", user.getName());
    }

    @Test
    public void testSetName(){
        User user = mockUser();
        user.setName("different");
        assertNotEquals(testName, user.getName());
        assertEquals("different",user.getName());
    }

    @Test
    public void testGetEmail(){
        User user = mockUser();
        assertEquals(testEmail, user.getEmail());
        assertEquals("test@test.test", user1.getEmail());
        assertEquals("www",user2.getEmail());
        assertNotEquals("www", user.getEmail());
    }

    @Test
    public void testSetEmail(){
        User user = mockUser();
        user.setEmail("123@gmail.com");
        assertNotEquals(testEmail, user.getEmail());
        assertEquals("123@gmail.com",user.getEmail());
    }

    @Test
    public void testGetPhone(){
        User user = mockUser();
        assertEquals(testPhone, user.getPhone());
        assertEquals("111", user1.getPhone());
        assertEquals("222", user2.getPhone());
        assertNotEquals("111", user.getPhone());
    }

    @Test
    public void testSetPhone(){
        User user = mockUser();
        user.setPhone("456");
        assertNotEquals(testPhone, user.getPhone());
        assertEquals("456", user.getPhone());
    }
}
