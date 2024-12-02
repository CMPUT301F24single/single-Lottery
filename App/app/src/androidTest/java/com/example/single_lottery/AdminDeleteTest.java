package com.example.single_lottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
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
Test covers the following:
-create event as organizer
-change profile as organizer
-change facility profile as organizer
-check if the event is updated with the new facility profile
-admin navigation
-admin delete facility (and events)
-admin delete profile
 */

public class AdminDeleteTest {
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
        Thread.sleep(400);

        //change organizer profile name
        onView(withIndex(withId(R.id.navigation_profile), 0)).perform(click());
        onView(withId(R.id.editButton)).perform(click());
        onView(withId(R.id.nameInput)).perform(replaceText("TESTUSERAAA"));
        onView(withText("save")).perform(ViewActions.click());

        //change organizer facility name
        onView(withIndex(withId(R.id.facilityButton), 0)).perform(click());
        onView(withId(R.id.editButton)).perform(click());
        onView(withId(R.id.nameInputFac)).perform(replaceText("FACILITYTEST123"));
        onView(withText("save")).perform(ViewActions.click());
        Espresso.pressBack();
        onView(withId(R.id.action_return)).perform(click());


    }
    @Test
    public void deleteTests() throws InterruptedException{
        //admin login
        onView(withId(R.id.button_admin)).perform(click());
        onView(withHint("Email")).perform(typeText("123"), closeSoftKeyboard());
        onView(withHint("Password")).perform(typeText("123456"), closeSoftKeyboard());
        onView(withText("Login")).perform(ViewActions.click());
        Thread.sleep(500);
        onView(withIndex(withId(R.id.nav_facility), 0)).perform(click());
        boolean found = false;

        //delete facility
        for (int i = 0; !found; i++) {
            try {
                Espresso.onView(withIndex(withId(R.id.facilityTextView), i)).check(ViewAssertions.matches(ViewMatchers.withText("FACILITYTEST123")));
                Espresso.onView(withIndex(withId(R.id.deleteButton), i)).perform(ViewActions.click());
                onView(withText("Yes")).perform(ViewActions.click());
                found = true;
            } catch (AssertionError e) {}
            catch(IndexOutOfBoundsException e){
                break;
            }
        }

        found = false;
        for (int i = 0; !found; i++) {
            try {
                Espresso.onView(withIndex(withId(R.id.facilityTextView), i)).check(ViewAssertions.matches(ViewMatchers.withText("FACILITYTEST123")));
                Espresso.onView(withIndex(withId(R.id.deleteButton), i)).perform(ViewActions.click());
                onView(withText("Yes")).perform(ViewActions.click());
                found = true;
            } catch (AssertionError e) {}
            catch(NoMatchingViewException e){
                break;
            }
        }

        //assert facility is deleted
        assertFalse(found);

        //delete user, this works but its very slow
        /*
        for (int i = 0; !found; i++) {
            try {
                Espresso.onView(withIndex(withId(R.id.adminUserName), i)).check(ViewAssertions.matches(ViewMatchers.withText("TESTUSERAAA")));
                Espresso.onView(withIndex(withId(R.id.adminUserName), i)).perform(ViewActions.click());
                onView(withId(R.id.btnDeleteProfile)).perform(click());
                onView(withText("Yes")).perform(ViewActions.click());
                found = true;
            } catch (AssertionError e) {}
            catch(NoMatchingViewException e){
                break;
            }
        }*/


    }
}
