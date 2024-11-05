package com.example.single_lottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import android.util.Log;
import android.view.View;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.installations.FirebaseInstallations;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class ProfileFragmentTest {
    private String installationId;

    //https://stackoverflow.com/questions/29378552/in-espresso-how-to-avoid-ambiguousviewmatcherexception-when-multiple-views-matc
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

    private String[] loadUserProfile(String installationId) {
        final String[] userProfile = new String[3];
        final CountDownLatch latch = new CountDownLatch(1);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = firestore.collection("users").document(installationId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    userProfile[0] = task.getResult().getString("name");
                    userProfile[1] = task.getResult().getString("email");
                    userProfile[2] = task.getResult().getString("phone");
                }
                else {
                    userProfile[0] = "Name";
                    userProfile[1] = "Email";
                    userProfile[2] = "Phone";
                }
                latch.countDown();
            }
        }).addOnFailureListener(e -> {
            Log.e("ProfileFragment", "failed to load user profile: " + e.getMessage());
            userProfile[0] = "Name";
            userProfile[1] = "Email";
            userProfile[2] = "Phone";
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return userProfile;
    }
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setup() throws InterruptedException {
        FirebaseInstallations.getInstance().getId()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        installationId = task.getResult();
                    } else {
                        Log.e("ProfileFragment", "failed to get installation id: " + task.getException());
                    }
                });
        onView(withId(R.id.button_user)).perform(click());
        Thread.sleep(500);
        onView(withIndex(withId(R.id.navigation_notifications), 0)).perform(click());
    }
    @Test
    public void currentUserDetails(){
        String[] details = loadUserProfile(installationId);
        onView(withId(R.id.nameTextView)).check(matches(withText(details[0])));
        onView(withId(R.id.emailTextView)).check(matches(withText(details[1])));
        onView(withId(R.id.phoneTextView)).check(matches(withText(details[2])));
    }

    @Test
    public void editedUserDetails(){
        String testName = "John299292";
        String testEmail = "icantsleep@gmail.com";
        String testPhone = "123123123";

        onView(withId(R.id.editButton)).perform(click());
        onView(withId(R.id.nameInput)).perform(replaceText(testName));
        onView(withId(R.id.emailInput)).perform(replaceText(testEmail));
        onView(withId(R.id.phoneInput)).perform(replaceText(testPhone));
        onView(withText("save")).perform(click());

        String[] details = loadUserProfile(installationId);
        onView(withId(R.id.nameTextView)).check(matches(withText(details[0])));
        onView(withId(R.id.emailTextView)).check(matches(withText(details[1])));
        onView(withId(R.id.phoneTextView)).check(matches(withText(details[2])));
    }
}
