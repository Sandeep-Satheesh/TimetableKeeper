 package com.example.timetablekeeper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.os.Bundle;

import com.example.timetablekeeper.adapters.ViewPagerAdapter;
import com.example.timetablekeeper.fragments.SubjectsFragment;
import com.example.timetablekeeper.fragments.JoinMeetingFragment;
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
        adapter.addFragment(new SubjectsFragment(), "Add a Subject");
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
 }