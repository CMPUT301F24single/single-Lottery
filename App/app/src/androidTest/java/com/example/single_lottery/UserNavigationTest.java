package com.example.single_lottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import android.view.View;
import androidx.test.core.app.ActivityScenario;
import com.example.single_lottery.ui.user.UserActivity;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

/**
 * Black box test class for testing the navigation functionality in the user interface.
 * This class verifies that navigation between different fragments works properly
 * and ensures that all UI elements are visible and properly displayed.
 *
 * Black box test for users.
 * Asserts that navigation works properly and fragments have visible details.
 *
 * @author Aaron Kim
 * @version 1.0
 */
public class UserNavigationTest {
    public ActivityScenario<UserActivity> scenario;

    /**
     * Custom matcher for handling multiple views with the same ID in the UI.
     * Used to select the correct view when multiple views share the same ID in XML layouts.
     *
     * @param matcher The base view matcher to use
     * @param index The index of the view to select when multiple matches exist
     * @return A TypeSafeMatcher that matches the view at the specified index
     */
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

    @Before
    public void setup() throws InterruptedException {
        scenario = ActivityScenario.launch(UserActivity.class);
        Thread.sleep(2000);
    }

    @Test
    public void fragmentTransitionTest() throws InterruptedException {
        onView(withId(R.id.recyclerViewEvents)).check(matches(isDisplayed()));
        Thread.sleep(1000);

        onView(withIndex(withId(R.id.navigation_notifications), 0)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.profileImageView)).check(matches(isDisplayed()));
        onView(withId(R.id.uploadButton)).check(matches(isDisplayed()));
        onView(withId(R.id.removeImageButton)).check(matches(isDisplayed()));
        onView(withId(R.id.nameLayout)).check(matches(isDisplayed()));
        onView(withId(R.id.emailLayout)).check(matches(isDisplayed()));
        onView(withId(R.id.phoneLayout)).check(matches(isDisplayed()));
        onView(withId(R.id.editButton)).check(matches(isDisplayed()));
    }
}
