package com.example.single_lottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.equalTo;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Utility class for custom picker actions in UI tests.
 *
 * This class provides methods to programmatically set dates and times in picker dialogs,
 * replacing the deprecated androidx.test.espresso:espresso-contrib.PickerActions.
 * These actions are essential for testing date and time selection in the UI.
 *
 * @author Aaron Kim
 * @version 1.0
 * Gpt4o: Make custom PickerActions class that select date and time of respective dialogboxes
 * //androidx.test.espresso:espresso-contrib.PickerActions is deprecated and has compatibility issues with running correctly on current environment
 * Functions in this class are used to manually set the date and time in a calendar/clock dialog box in a UI test.
 */
/*
 * Gpt4o: Make custom PickerActions class that select date and time of respective dialogboxes
 * //androidx.test.espresso:espresso-contrib.PickerActions is deprecated and has compatibility issues with running correctly on current environment
 * Functions in this class are used to manually set the date and time in a calendar/clock dialog box in a UI test.
 */
class PickerActions {
    /**
     * Creates a ViewAction that sets a specific date in a DatePicker.
     *
     * @param year The year to set
     * @param month The month to set (0-11)
     * @param dayOfMonth The day of month to set
     * @return ViewAction that sets the specified date
     */
    public static ViewAction setDate(final int year, final int month, final int dayOfMonth) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }
            @Override
            public String getDescription() {
                return "Set the date on the DatePicker";
            }
            @Override
            public void perform(UiController uiController, View view) {
                DatePicker datePicker = (DatePicker) view;
                datePicker.updateDate(year, month, dayOfMonth);
            }
        };
    }
    /**
     * Creates a ViewAction that sets a specific time in a TimePicker.
     *
     * @param hour The hour to set (0-23)
     * @param minute The minute to set (0-59)
     * @return ViewAction that sets the specified time
     */
    public static ViewAction setTime(final int hour, final int minute) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Set the time on the TimePicker";
            }

            @Override
            public void perform(UiController uiController, View view) {
                TimePicker timePicker = (TimePicker) view;
                timePicker.setHour(hour);
                timePicker.setMinute(minute);
            }
        };
    }
}

/*
 * Black box testing for creating and viewing an event as an organizer.
    Things to adjust for while app is still in development:
    -test case launch will change as the app is able to automatically choose device as one of: {user, organizer, admin}
    -check deletion of events functionality (if applicable to an organizer)
    -check for correct inputs (e.g. a lottery date shouldn't exceed the event date)
 */

public class OrganizerEventTest {
    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Initial setup for each test.
     * Navigates to the organizer view and selects the new event creation screen.
     *
     * @throws InterruptedException if the thread sleep is interrupted
     */
    @Before
    public void setup() throws InterruptedException {
        onView(withId(R.id.button_organizer)).perform(click());
        Thread.sleep(2000);
        onView(withIndex(withId(R.id.navigation_new), 0)).perform(click());
        Thread.sleep(2000);
    }

    /**
     * Tests the complete workflow of creating a new event as an organizer.
     * Validates:
     * - Event detail input (name, description, waiting list size, lottery count)
     * - Date and time selection for event time, registration deadline, and lottery time
     * - Event creation and visibility in the events list
     *
     * @throws InterruptedException if the thread sleep is interrupted
     */
    @Test
    public void newEvent() throws InterruptedException {
        String testEventName = "test case event";
        String testEventDescription = "Christmas event!";
        String testWaitingListCount = "100";
        String testLotteryCount = "50";

        //set string fields
        onView(withId(R.id.eventNameEditText)).perform(replaceText(testEventName));
        onView(withId(R.id.eventDescriptionEditText)).perform(replaceText(testEventDescription));
        onView(withId(R.id.waitingListCountEditText)).perform(replaceText(testWaitingListCount));
        onView(withId(R.id.lotteryCountEditText)).perform(replaceText(testLotteryCount));

        //set dates
        onView(withId(R.id.eventTimeTextView)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName())))
                .perform(PickerActions.setDate(2024, 11, 25));
        onView(withText("OK")).perform(ViewActions.click());
        Thread.sleep(250);
        onView(ViewMatchers.withClassName(equalTo(TimePicker.class.getName())))
                .perform(PickerActions.setTime(12,0));
        onView(withText("OK")).perform(ViewActions.click());
        Thread.sleep(250);

        onView(withId(R.id.registrationDeadlineTextView)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName())))
                .perform(PickerActions.setDate(2024, 11, 20));
        onView(withText("OK")).perform(ViewActions.click());
        Thread.sleep(250);
        onView(ViewMatchers.withClassName(equalTo(TimePicker.class.getName())))
                .perform(PickerActions.setTime(23,59));
        onView(withText("OK")).perform(ViewActions.click());
        Thread.sleep(250);

        onView(withId(R.id.lotteryTimeTextView)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName())))
                .perform(PickerActions.setDate(2024, 11, 24));
        onView(withText("OK")).perform(ViewActions.click());
        Thread.sleep(250);
        onView(ViewMatchers.withClassName(equalTo(TimePicker.class.getName())))
                .perform(PickerActions.setTime(13,0));
        onView(withText("OK")).perform(ViewActions.click());
        Thread.sleep(250);

        //create event
        onView(withId(R.id.createEventButton)).perform(click());
        onView(withId(R.id.backButton)).perform(click());
        onView(withIndex(withId(R.id.navigation_home), 0)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed())); //assert that events list is visible
    }

}
