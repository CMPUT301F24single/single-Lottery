package com.example.single_lottery;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class EventModelTest {

    private EventModel eventModel;

    @Before
    public void setUp() {
        eventModel = new EventModel();
    }

    @Test
    public void testGetSetEventName() {
        String eventName = "Test Event";
        eventModel.setEventName(eventName);
        assertEquals("Test Event", eventModel.getName());
    }

    @Test
    public void testGetSetOrganizerDeviceID() {
        String organizerDeviceID = "1234ABC";
        eventModel.setOrganizerDeviceID(organizerDeviceID);
        assertEquals("1234ABC", eventModel.getOrganizerDeviceID());
    }

    @Test
    public void testGetSetUserDeviceID() {
        String userDeviceID = "5678XYZ";
        eventModel.setUserDeviceID(userDeviceID);
        assertEquals("5678XYZ", eventModel.getUserDeviceID());
    }

    @Test
    public void testGetSetTime() {
        String time = "2024-12-01T10:00:00";
        eventModel.setTime(time);
        assertEquals("2024-12-01T10:00:00", eventModel.getTime());
    }

    @Test
    public void testGetSetRegistrationDeadline() {
        String registrationDeadline = "2024-11-30T23:59:59";
        eventModel.setRegistrationDeadline(registrationDeadline);
        assertEquals("2024-11-30T23:59:59", eventModel.getRegistrationDeadline());
    }

    @Test
    public void testGetSetLotteryTime() {
        String lotteryTime = "2024-12-01T11:00:00";
        eventModel.setLotteryTime(lotteryTime);
        assertEquals("2024-12-01T11:00:00", eventModel.getLotteryTime());
    }

    @Test
    public void testGetSetWaitingListCount() {
        int waitingListCount = 5;
        eventModel.setWaitingListCount(waitingListCount);
        assertEquals(5, eventModel.getWaitingListCount());
    }

    @Test
    public void testGetSetLotteryCount() {
        int lotteryCount = 100;
        eventModel.setLotteryCount(lotteryCount);
        assertEquals(100, eventModel.getLotteryCount());
    }

    @Test
    public void testGetSetPosterUrl() {
        String posterUrl = "https://example.com/poster.jpg";
        eventModel.setPosterUrl(posterUrl);
        assertEquals("https://example.com/poster.jpg", eventModel.getPosterUrl());
    }

    @Test
    public void testGetSetEventId() {
        String eventId = "event123";
        eventModel.setEventId(eventId);
        assertEquals("event123", eventModel.getEventId());
    }

    @Test
    public void testGetSetDescription() {
        String description = "This is a test event description.";
        eventModel.setDescription(description);
        assertEquals("This is a test event description.", eventModel.getDescription());
    }

    @Test
    public void testGetSetProfileImageUrl() {
        String profileImageUrl = "https://example.com/profile.jpg";
        eventModel.setProfileImageUrl(profileImageUrl);
        assertEquals("https://example.com/profile.jpg", eventModel.getProfileImageUrl());
    }

    @Test
    public void testGetSetEmail() {
        String email = "test@example.com";
        eventModel.setEmail(email);
        assertEquals("test@example.com", eventModel.getEmail());
    }

    @Test
    public void testGetSetPhone() {
        String phone = "123-456-7890";
        eventModel.setPhone(phone);
        assertEquals("123-456-7890", eventModel.getPhone());
    }

    @Test
    public void testGetSetInfo() {
        String info = "Additional event information.";
        eventModel.setInfo(info);
        assertEquals("Additional event information.", eventModel.getInfo());
    }

    @Test
    public void testGetSetFacility() {
        String facility = "Venue 1";
        eventModel.setFacility(facility);
        assertEquals("Venue 1", eventModel.getFacility());
    }

    @Test
    public void testGetSetRequiresLocation() {
        boolean requiresLocation = true;
        eventModel.setRequiresLocation(requiresLocation);
        assertTrue(eventModel.isRequiresLocation());
    }

    @Test
    public void testNoArgsConstructor() {
        EventModel eventModel1 = new EventModel();
        assertNotNull(eventModel1);
    }

    @Test
    public void testCompleteEventModel() {
        EventModel eventModel2 = new EventModel();
        eventModel2.setEventName("Event 2");
        eventModel2.setOrganizerDeviceID("A1B2C3");
        eventModel2.setUserDeviceID("D4E5F6");
        eventModel2.setTime("2024-12-05T10:30:00");
        eventModel2.setRegistrationDeadline("2024-12-01T23:59:59");
        eventModel2.setLotteryTime("2024-12-05T12:00:00");
        eventModel2.setWaitingListCount(10);
        eventModel2.setLotteryCount(50);
        eventModel2.setPosterUrl("https://example.com/event2poster.jpg");
        eventModel2.setEventId("event2");
        eventModel2.setDescription("This is a detailed description of event 2.");
        eventModel2.setProfileImageUrl("https://example.com/event2profile.jpg");
        eventModel2.setEmail("event2@example.com");
        eventModel2.setPhone("987-654-3210");
        eventModel2.setInfo("Event 2 specific info.");
        eventModel2.setFacility("Venue 2");
        eventModel2.setRequiresLocation(false);

        assertEquals("Event 2", eventModel2.getName());
        assertEquals("A1B2C3", eventModel2.getOrganizerDeviceID());
        assertEquals("D4E5F6", eventModel2.getUserDeviceID());
        assertEquals("2024-12-05T10:30:00", eventModel2.getTime());
        assertEquals("2024-12-01T23:59:59", eventModel2.getRegistrationDeadline());
        assertEquals("2024-12-05T12:00:00", eventModel2.getLotteryTime());
        assertEquals(10, eventModel2.getWaitingListCount());
        assertEquals(50, eventModel2.getLotteryCount());
        assertEquals("https://example.com/event2poster.jpg", eventModel2.getPosterUrl());
        assertEquals("event2", eventModel2.getEventId());
        assertEquals("This is a detailed description of event 2.", eventModel2.getDescription());
        assertEquals("https://example.com/event2profile.jpg", eventModel2.getProfileImageUrl());
        assertEquals("event2@example.com", eventModel2.getEmail());
        assertEquals("987-654-3210", eventModel2.getPhone());
        assertEquals("Event 2 specific info.", eventModel2.getInfo());
        assertEquals("Venue 2", eventModel2.getFacility());
        assertFalse(eventModel2.isRequiresLocation());
    }
}
