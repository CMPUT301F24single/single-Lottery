package com.example.single_lottery;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.example.single_lottery.ui.organizer.Facility;

public class FacilityTest {

    private Facility facility;

    @Before
    public void setUp() {
        facility = new Facility("Test Facility", "Test Location", "oldUrl");
    }

    @Test
    public void testGetName() {
        assertEquals("Test Facility", facility.getName());

        facility.setName("Updated Facility");
        assertEquals("Updated Facility", facility.getName());

        facility.setName("");
        assertEquals("", facility.getName());

        facility.setName(null);
        assertNull(facility.getName());

        facility.setName("Back to Facility");
        assertEquals("Back to Facility", facility.getName());
    }

    @Test
    public void testGetLocation() {
        assertEquals("Test Location", facility.getLocation());

        facility.setLocation("New Location");
        assertEquals("New Location", facility.getLocation());

        facility.setLocation("");
        assertEquals("", facility.getLocation());

        facility.setLocation(null);
        assertNull(facility.getLocation());

        facility.setLocation("Updated Location");
        assertEquals("Updated Location", facility.getLocation());
    }

    @Test
    public void testGetProfileImageUrl() {
        assertEquals("oldUrl", facility.getProfileImageUrl());

        facility.setProfileImageUrl("myUrl");
        assertEquals("myUrl", facility.getProfileImageUrl());

        facility.setProfileImageUrl("");
        assertEquals("", facility.getProfileImageUrl());

        facility.setProfileImageUrl(null);
        assertNull(facility.getProfileImageUrl());

        facility.setProfileImageUrl("newUrl");
        assertEquals("newUrl", facility.getProfileImageUrl());
    }

    @Test
    public void testFacilityConstructor() {
        Facility newFacility = new Facility("Constructor Facility", "Constructor Location", "blank");
        assertEquals("Constructor Facility", newFacility.getName());
        assertEquals("Constructor Location", newFacility.getLocation());
        assertEquals("blank", newFacility.getProfileImageUrl());
    }

    @Test
    public void testSetNameNull() {
        facility.setName(null);
        assertNull(facility.getName());

        facility.setName(null);
        assertNull(facility.getName());
    }

    @Test
    public void testSetLocation_null() {
        facility.setLocation(null);
        assertNull(facility.getLocation());

        facility.setLocation(null);
        assertNull(facility.getLocation());
    }

    @Test
    public void testSetProfileImageUrl_null() {
        facility.setProfileImageUrl(null);
        assertNull(facility.getProfileImageUrl());

        facility.setProfileImageUrl(null);
        assertNull(facility.getProfileImageUrl());
    }

    @Test
    public void testSetName_empty() {
        facility.setName("");
        assertEquals("", facility.getName());

        facility.setName("");
        assertEquals("", facility.getName());
    }

    @Test
    public void testSetLocation_empty() {
        facility.setLocation("");
        assertEquals("", facility.getLocation());

        facility.setLocation("");
        assertEquals("", facility.getLocation());
    }

    @Test
    public void testSetProfileImageUrl_empty() {
        facility.setProfileImageUrl("");
        assertEquals("", facility.getProfileImageUrl());

        facility.setProfileImageUrl("");
        assertEquals("", facility.getProfileImageUrl());
    }

    @Test
    public void testEmptyFields() {
        facility.setName("");
        facility.setLocation("");
        facility.setProfileImageUrl("");

        assertEquals("", facility.getName());
        assertEquals("", facility.getLocation());
        assertEquals("", facility.getProfileImageUrl());
    }

    @Test
    public void testSetName_withValidData() {
        facility.setName("Valid Facility Name");
        assertEquals("Valid Facility Name", facility.getName());
    }

    @Test
    public void testSetLocation_withValidData() {
        facility.setLocation("Valid Facility Location");
        assertEquals("Valid Facility Location", facility.getLocation());
    }

    @Test
    public void testSetProfileImageUrl_withValidData() {
        facility.setProfileImageUrl("newValid");
        assertEquals("newValid", facility.getProfileImageUrl());
    }

    @Test
    public void testGettersAndSetters() {
        facility.setName("New Facility");
        assertEquals("New Facility", facility.getName());

        facility.setLocation("New Location");
        assertEquals("New Location", facility.getLocation());

        facility.setProfileImageUrl("newValid");
        assertEquals("newValid", facility.getProfileImageUrl());
    }
}

