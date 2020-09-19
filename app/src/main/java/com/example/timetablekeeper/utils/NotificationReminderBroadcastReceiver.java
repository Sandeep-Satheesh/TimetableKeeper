package com.example.timetablekeeper.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.timetablekeeper.MainActivity;
import com.example.timetablekeeper.models.SubjectObj;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Date;

public class NotificationReminderBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //display the notif
        String subjDetails = intent.getStringExtra("subjDetails");
        SubjectObj obj = new Gson().fromJson(subjDetails, SubjectObj.class);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = CommonUtils.prepareNotification(true, "Ready for next class?", obj.getSubShortForm() + " has started! Join on time to avoid losing attendance!\nFaculty: " + obj.getFacultyName(), context, new Intent(context, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP), obj);
        notificationManager.notify(1, notification);

        ArrayList<SubjectObj> objs = new Gson().fromJson(SharedPref.getString(context, "remainingSubjs"), new TypeToken<ArrayList<SubjectObj>>(){}.getType());
        ArrayList<Integer> hours = new Gson().fromJson(SharedPref.getString(context, "remainingHrs"), new TypeToken<ArrayList<Integer>>(){}.getType());
        if (objs == null || objs.size() == 0) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Date startTime = CommonUtils.getStartTimeFromHour(context, hours.get(0));
        obj = objs.get(0);
        Intent i = new Intent(context, NotificationReminderBroadcastReceiver.class).putExtra("subjDetails", new Gson().toJson(obj));
        PendingIntent notificationPendingIntent = PendingIntent.getBroadcast(context, 2, i, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTime.getTime(), notificationPendingIntent);
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, startTime.getTime(), notificationPendingIntent);

        hours.remove(0);
        objs.remove(0);
        if (objs.size() > 0) {
            String s = new Gson().toJson(objs);
            String s1 = new Gson().toJson(hours);
            SharedPref.putString(context, "remainingSubjs", s);
            SharedPref.putString(context, "remainingHrs", s1);
        }
    }
}
