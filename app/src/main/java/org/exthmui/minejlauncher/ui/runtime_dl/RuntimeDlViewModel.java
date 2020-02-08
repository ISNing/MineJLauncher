package org.exthmui.minejlauncher.ui.runtime_dl;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RuntimeDlViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public RuntimeDlViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}