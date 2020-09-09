 package com.example.timetablekeeper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.timetablekeeper.adapters.ViewPagerAdapter;
import com.example.timetablekeeper.fragments.SubjectsFragment;
import com.example.timetablekeeper.fragments.JoinMeetingFragment;
import com.example.timetablekeeper.utils.SharedPref;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

 public class MainActivity extends AppCompatActivity {
     ViewPager viewPager;
     ViewPagerAdapter adapter;
     SmartTabLayout viewPagerTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewpager);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new JoinMeetingFragment(), "Join A Class");
        adapter.addFragment(new SubjectsFragment(), "Manage Subjects");
        viewPager.setAdapter(adapter);

        viewPagerTab = findViewById(R.id.viewpagertab);
        viewPagerTab.setViewPager(viewPager);
    }

     @Override
     public void onBackPressed() {
        if (viewPager.getCurrentItem() == 1) viewPager.setCurrentItem(0);
        else new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setTitle("Close app?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                })
                .setNegativeButton("No", null).show();
     }

     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.app_options_menu, menu);
        return true;
     }

     @Override
     public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings: break;
        }
         return super.onOptionsItemSelected(item);
     }
 }