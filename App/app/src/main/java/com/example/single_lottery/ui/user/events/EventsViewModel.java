package com.example.single_lottery.ui.user.events;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public EventsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is event");
    }

    public LiveData<String> getText() {
        return mText;
    }

    // 判断活动是否结束
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