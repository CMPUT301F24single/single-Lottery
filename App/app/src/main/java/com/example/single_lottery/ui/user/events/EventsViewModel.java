package com.example.single_lottery.ui.user.events;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ViewModel for managing events data and state.
 * Handles event time validation and text display.
 *
 * @author [Jingyao Gu]
 * @version 1.0
 */
public class EventsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public EventsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is event");
    }

    public LiveData<String> getText() {
        return mText;
    }

    /**
     * Checks if event has ended by comparing current time with event time.
     * Parses date string in "yyyy-MM-dd HH:mm" format.
     *
     * @param eventTime Event time string in "yyyy-MM-dd HH:mm" format
     * @return true if event has ended, false otherwise or if parsing fails
     */
    private boolean isEventEnded(String eventTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date eventDate = sdf.parse(eventTime);
            return new Date().after(eventDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

}