 package com.example.timetablekeeper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
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

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS}, 1);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (checkSelfPermission(Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND}, 1);
                }
            }
        }*/
        if (!SharedPref.getBoolean(getApplicationContext(), "autoStartDiagShown")) {
            //this will open auto start screen where user can enable permission for the app
            final String manufacturer = Build.MANUFACTURER;
            switch (manufacturer) {
                case "xiaomi":
                case "asus":
                case "oppo":
                case "vivo":
                case "huawei":
                case "honor":
                case "Letv":
                    new AlertDialog.Builder(this)
                            .setTitle("Please grant background running permissions")
                            .setMessage("This app needs to run in background for you to be able to receive in-time notifications about whether" +
                                    " the next period has started or not. Currently there is no way to check whether these permissions have been granted by the system (you)" +
                                    ", and we can only request you to enable these permissions, if you already haven't.\nHence, you won't see this dialog again, and will have to manually" +
                                    " go to the auto-start permissions page in your phone Settings to enable it.\n\nWould you like to be redirected to the settings page now?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if ("xiaomi".equalsIgnoreCase(manufacturer))
                                        startActivity(new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")));
                                    else if ("oppo".equalsIgnoreCase(manufacturer))
                                        startActivity(new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")));
                                    else if ("vivo".equalsIgnoreCase(manufacturer))
                                        startActivity(new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")));
                                    else if ("Letv".equalsIgnoreCase(manufacturer))
                                        startActivity(new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")));
                                    else if ("Honor".equalsIgnoreCase(manufacturer))
                                        startActivity(new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")));
                                    else if ("asus".equalsIgnoreCase(manufacturer))
                                        startActivity(new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.powersaver.PowerSaverSettings")));
                                    else if ("nokia".equalsIgnoreCase(manufacturer))
                                        startActivity(new Intent().setComponent(new ComponentName("com.evenwell.powersaving.g3", "com.evenwell.powersaving.g3.exception.PowerSaverExceptionActivity")));
                                }
                            }).setNegativeButton("No", null)
                            .show();
                    SharedPref.putBoolean(getApplicationContext(), "scheduleUpdatedFlg", true);
                    break;
                default:
                    new AlertDialog.Builder(this)
                            .setTitle("Please grant background running permissions")
                            .setMessage("This app needs to run in background for you to be able to receive in-time notifications about whether" +
                                    " the next period has started or not. Currently there is no way to check whether these permissions have been granted by the system (you)" +
                                    ", and we can only request you to enable these permissions, if you already haven't.\nHence, you won't see this dialog again, and will have to manually" +
                                    " go to the auto-start permissions page in your phone Settings to enable it.")
                            .setPositiveButton("OK", null)
                            .show();
            }
        }
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