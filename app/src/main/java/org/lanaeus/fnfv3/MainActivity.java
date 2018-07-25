package org.lanaeus.fnfv3;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private Toolbar mToolbar;
    private ViewPager viewPager;
    private MainPagerAdapter viewPagerAdapter;

    private FirebaseUser currentUser;
    private DatabaseReference mUserRef;

    private static TabLayout mTabLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final int[] ICONS = new int[]{
                R.drawable.tab_request_icon,
                R.drawable.tab_chat_icon,
                R.drawable.tab_friends_icon};

        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);

        //Tabs
        viewPager = (ViewPager) findViewById(R.id.main_tabPager);
        viewPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setCurrentItem(1);

        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(viewPager);
        mTabLayout.getTabAt(0).setIcon(ICONS[0]);
        mTabLayout.getTabAt(1).setIcon(ICONS[1]);
        mTabLayout.getTabAt(2).setIcon(ICONS[2]);

        startService(new Intent(this, MyService.class)); //background service for tracking location.

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("FnF");
        mToolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid());


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        if (currentUser == null) {
            sendToLogin();
        } else {
            mUserRef.child("online").setValue("true"); //to make the user online -- do this for all activity
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (currentUser != null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }

    }

    private void sendToLogin() {
        startActivity(new Intent(MainActivity.this, LogInActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem item = menu.findItem(R.id.main_reminder_btn);
        item.setVisible(true);
        MenuItem item2 = menu.findItem(R.id.main_settings_btn);
        item2.setVisible(true);

        return true;
    }


    //to add items on the options menu in main activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_reminder_btn){
//            Intent intent = new Intent(MainActivity.this,ReminderMapsActivity.class);
//            startActivity(intent);

            ReminderCustomDialog dialog = new ReminderCustomDialog();
            dialog.show(getFragmentManager(),"MyCustomDialog");
        }
        if (item.getItemId() == R.id.main_settings_btn){
            Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
        }

        return true;
    }

    public static void showTabLayout() {
        mTabLayout.setVisibility(View.VISIBLE);
    }

    public static void hideTabLayout() {
        mTabLayout.setVisibility(View.GONE);

    }


}


