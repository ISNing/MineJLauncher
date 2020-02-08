package org.exthmui.minejlauncher.ui.login;

import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import org.exthmui.minejlauncher.Constants;
import org.exthmui.minejlauncher.MainActivity;
import org.exthmui.minejlauncher.R;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText apiUrlEditText;
    private Button loginButton;
    private ProgressBar loadingProgressBar;
    private TextView authModeText;
    private Spinner authModeChooser;

    private static LoginActivity activity;

    private int loginModeFlag = 0;

    public static LoginActivity getLoginActivity(){
        return activity;
    }

    public LoginViewModel getLoginViewModel() {
        return loginViewModel;
    }

    public EditText getUsernameEditText() {
        return usernameEditText;
    }

    public EditText getPasswordEditText() {
        return passwordEditText;
    }

    public EditText getApiUrlEditText() {
        return apiUrlEditText;
    }

    public Button getLoginButton() {
        return loginButton;
    }

    public ProgressBar getLoadingProgressBar() {
        return loadingProgressBar;
    }

    public Spinner getAuthModeChooser() {
        return authModeChooser;
    }

    public TextView getAuthModeText() {
        return authModeText;
    }

    public int getLoginModeFlag() {
        return loginModeFlag;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocused){
        authModeChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:{
                        loginModeFlag = Constants.LOGIN_FLAG_OFFICIAL;

                        usernameEditText.setVisibility(View.VISIBLE);
                        passwordEditText.setVisibility(View.VISIBLE);
                        apiUrlEditText.setVisibility(View.GONE);

                        usernameEditText.setImeOptions(EditorInfo.IME_ACTION_NONE);
                        passwordEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        apiUrlEditText.setImeOptions(EditorInfo.IME_ACTION_NONE);

                        usernameEditText.setOnEditorActionListener(null);
                        apiUrlEditText.setOnEditorActionListener(null);
                        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

                            @Override
                            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                if (actionId == EditorInfo.IME_ACTION_DONE) {
                                    loginViewModel.startLogin(usernameEditText.getText().toString(),
                                            passwordEditText.getText().toString(),
                                            loginModeFlag == Constants.LOGIN_FLAG_OFFICIAL ?
                                                    getString(R.string.auth_mojang_api_url) :
                                                    apiUrlEditText.getText().toString());
                                }
                                return false;
                            }
                        });
                        break;
                    }
                    case 1:{
                        loginModeFlag = Constants.LOGIN_FLAG_AUTHLIB_INJECTOR;

                        usernameEditText.setVisibility(View.VISIBLE);
                        passwordEditText.setVisibility(View.VISIBLE);
                        apiUrlEditText.setVisibility(View.VISIBLE);

                        usernameEditText.setImeOptions(EditorInfo.IME_ACTION_NONE);
                        passwordEditText.setImeOptions(EditorInfo.IME_ACTION_NONE);
                        apiUrlEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);

                        usernameEditText.setOnEditorActionListener(null);
                        passwordEditText.setOnEditorActionListener(null);
                        apiUrlEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

                            @Override
                            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                if (actionId == EditorInfo.IME_ACTION_DONE) {
                                    loginViewModel.startLogin(usernameEditText.getText().toString(),
                                            passwordEditText.getText().toString(),
                                            loginModeFlag == Constants.LOGIN_FLAG_OFFICIAL ?
                                                    getString(R.string.auth_mojang_api_url) :
                                                    apiUrlEditText.getText().toString());
                                }
                                return false;
                            }
                        });
                        break;
                    }
                    case 2:{
                        loginModeFlag = Constants.LOGIN_FLAG_OFFLINE;

                        usernameEditText.setVisibility(View.VISIBLE);
                        passwordEditText.setVisibility(View.GONE);
                        apiUrlEditText.setVisibility(View.GONE);

                        usernameEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        passwordEditText.setImeOptions(EditorInfo.IME_ACTION_NONE);
                        apiUrlEditText.setImeOptions(EditorInfo.IME_ACTION_NONE);

                        passwordEditText.setOnEditorActionListener(null);
                        apiUrlEditText.setOnEditorActionListener(null);
                        usernameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

                            @Override
                            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                if (actionId == EditorInfo.IME_ACTION_DONE) {
                                    loginViewModel.startLogin(usernameEditText.getText().toString(),
                                            passwordEditText.getText().toString(),
                                            loginModeFlag == Constants.LOGIN_FLAG_OFFICIAL ?
                                                    getString(R.string.auth_mojang_api_url) :
                                                    apiUrlEditText.getText().toString());
                                }
                                return false;
                            }
                        });
                        break;
                    }
                    default:{
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //ignore
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;

        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        apiUrlEditText = findViewById(R.id.login_auth_apiurl);
        loginButton = findViewById(R.id.login);
        loadingProgressBar = findViewById(R.id.loading);
        authModeChooser = findViewById(R.id.login_auth_mode_chooser);
        authModeText = findViewById(R.id.login_text_loginmode);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
                if (loginFormState.getApiUrlError() != null) {
                    apiUrlEditText.setError(getString(loginFormState.getApiUrlError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    // Complete and destroy login activity once successful
                    updateUiWithUser(loginResult.getSuccess());
                    finish();
                }
                setResult(Activity.RESULT_OK);


                //finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getVisibility() == View.GONE ?
                                "AaBbCc123456" :
                                passwordEditText.getText().toString(),
                        loginModeFlag == Constants.LOGIN_FLAG_OFFICIAL ?
                                getString(R.string.auth_mojang_api_url) :
                                apiUrlEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.startLogin(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString(),
                            loginModeFlag == Constants.LOGIN_FLAG_OFFICIAL ?
                                    getString(R.string.auth_mojang_api_url) :
                                    apiUrlEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.startLogin(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(),
                        loginModeFlag == Constants.LOGIN_FLAG_OFFICIAL ?
                                getString(R.string.auth_mojang_api_url) :
                                apiUrlEditText.getText().toString());
            }
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        PreferenceManager.getDefaultSharedPreferences(activity).edit().putInt("login_mode_flag", loginModeFlag);
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        Snackbar.make(MainActivity.getMainActivity().findViewById(R.id.drawer_layout), welcome, Snackbar.LENGTH_SHORT).show();
    }

    private void showLoginFailed(String errorString) {
        PreferenceManager.getDefaultSharedPreferences(activity).edit().remove("login_mode_flag");
        PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit().putBoolean("logged", false);
        Snackbar snackbar = Snackbar.make(LoginActivity.getLoginActivity().findViewById(R.id.login_root), errorString, Snackbar.LENGTH_LONG);
        View view = snackbar.getView();
        ((TextView) view.findViewById(com.google.android.material.R.id.snackbar_text)).setMaxLines(9999);
        snackbar.show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit().putBoolean("logged", false);
        Snackbar.make(LoginActivity.getLoginActivity().findViewById(R.id.login_root), errorString, Snackbar.LENGTH_LONG).show();
    }
}
