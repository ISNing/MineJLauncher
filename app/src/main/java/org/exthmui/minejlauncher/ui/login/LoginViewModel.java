package org.exthmui.minejlauncher.ui.login;

import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.util.Patterns;

import org.exthmui.minejlauncher.MainActivity;
import org.exthmui.minejlauncher.data.LoginDataSource;
import org.exthmui.minejlauncher.data.LoginRepository;
import org.exthmui.minejlauncher.data.Result;
import org.exthmui.minejlauncher.data.model.LoggedInUser;
import org.exthmui.minejlauncher.R;
import org.exthmui.minejlauncher.model.OnVariableChangeListener;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void startLogin(String username, String password, String apiurl) {
        // can be launched in a separate asynchronous job
        loginRepository.startLogin(username, password, apiurl);
        while(LoginDataSource.getInstance().getLogger() == null) {
            //hold
        }
        LoginDataSource.getInstance().getLogger().setOnResultChangeListener(new OnVariableChangeListener() {
            @Override
            public void OnVariableChange() {
                login();
            }
        });
    }

    public void login() {
        Result<LoggedInUser> result = loginRepository.login();

        if (result instanceof Result.Success) {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            loginResult.setValue(new LoginResult(new LoggedInUserView(data.getSelectedProfile().name)));
        } else {
            loginResult.setValue(new LoginResult(LoginActivity.getLoginActivity().getString(R.string.login_failed) + "\n" +
                    ((Result.Error) result).getError().toString()));
        }
    }

    public void loginDataChanged(String username, String password, String apiurl) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password, null));
        } else if (!isApiUrlValid(apiurl)) {
            loginFormState.setValue(new LoginFormState(null, null, R.string.invalid_apiurl));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }else return Patterns.EMAIL_ADDRESS.matcher(username).matches();
    }

    // password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    // apiurl validation check
    private boolean isApiUrlValid(String apiurl) {
        if (apiurl == null) {
            return false;
        }else return Patterns.WEB_URL.matcher(apiurl).matches();
    }
}
