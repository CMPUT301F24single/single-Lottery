package com.example.single_lottery.ui.user.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
/**
 * ViewModel class for managing profile screen data.
 * Provides data and state management for the profile interface.
 *
 * @version 1.0
 */
public class ProfileViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ProfileViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is profile");
    }

    public LiveData<String> getText() {
        return mText;
    }
}