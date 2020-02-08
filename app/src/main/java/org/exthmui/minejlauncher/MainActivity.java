package org.exthmui.minejlauncher;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;

import org.exthmui.minejlauncher.data.LoginDataSource;
import org.exthmui.minejlauncher.data.LoginRepository;
import org.exthmui.minejlauncher.data.model.LoggedInUser;
import org.exthmui.minejlauncher.ui.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private static MainActivity activity;
    public boolean refreshedToken = false;
    public int authStatusCode;

    @Override
    public void onWindowFocusChanged(boolean hasFocused){
        refreshUserStatus();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Let's start!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_lunch, R.id.nav_runtime_dl, R.id.nav_game_dl,
                R.id.nav_settings, R.id.nav_open_source, R.id.nav_about)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public static MainActivity getMainActivity() {
        return activity;
    }

    public void refreshUserStatus(){
        refreshUserStatus(LoginRepository.getInstance(LoginDataSource.getInstance()).getLoggedInUeser());
    }

    public void refreshUserStatus(LoggedInUser user){
        final LoginRepository loginRepository = LoginRepository.getInstance(LoginDataSource.getInstance());
        ConstraintLayout nav_header = (ConstraintLayout) findViewById(R.id.nav_view).findViewById(R.id.nav_header);
        Button loginButton = (Button) nav_header.findViewById(R.id.nav_header_login_button);
        TextView title = (TextView) nav_header.findViewById(R.id.nav_header_text_title);
        TextView subtitle = (TextView) nav_header.findViewById(R.id.nav_header_text_subtitle);
        if(!loginRepository.isLoggedIn()){
            loginButton.setText(R.string.login_login);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            });
            title.setText(getString(R.string.nav_header_title));
            subtitle.setText(getString(R.string.nav_header_subtitle));
        }else {
            loginButton.setText(R.string.login_logout);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loginRepository.logout();
                }
            });
            title.setText(user.getSelectedProfile().name);
            subtitle.setText(user.getUsername());
        }

    }
}
