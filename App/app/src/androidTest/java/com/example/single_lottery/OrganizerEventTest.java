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

/*
 * Gpt4o: Make custom PickerActions class that select date and time of respective dialogboxes
 * //androidx.test.espresso:espresso-contrib.PickerActions is deprecated and has compatibility issues with running correctly on current environment
 * Functions in this class are used to manually set the date and time in a calendar/clock dialog box in a UI test.
 */
class PickerActions {
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

    @Before
    public void setup() throws InterruptedException {
        onView(withId(R.id.button_organizer)).perform(click());
        Thread.sleep(2000);
        onView(withIndex(withId(R.id.navigation_new), 0)).perform(click());
        Thread.sleep(2000);
    }

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
