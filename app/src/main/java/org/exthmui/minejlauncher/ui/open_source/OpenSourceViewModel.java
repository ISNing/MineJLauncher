package org.exthmui.minejlauncher.ui.open_source;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class OpenSourceViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public OpenSourceViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is share fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}