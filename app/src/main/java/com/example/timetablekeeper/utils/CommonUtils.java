package com.example.timetablekeeper.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.timetablekeeper.JoinMeetFromPCActivity;
import com.example.timetablekeeper.R;
import com.example.timetablekeeper.models.SubjectObj;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

public class CommonUtils {
    public static int determineCurrentPeriodNumber(Context c, long currentTimeMillis) {
        /*
        Current time is stored in 24 hr format as 4 - digit integer, as the time itself
        eg: 23:45 is stored as 2345, 01:00 as 100, 00:01 as 1, etc.
        */
        Date d = new Date(currentTimeMillis);
        int time = d.getHours() * 100 + d.getMinutes();
        int totalHrs = SharedPref.getInt(c, "totalHrs");
        if (time < SharedPref.getInt(c, "hr1", "start"))
            return -1;
        for (int i = 1; i <= totalHrs; i++) {
            int start = SharedPref.getInt(c, "hr" + i, "start"),
                    end = SharedPref.getInt(c, "hr" + i, "end");
            if (time >= start && time <= end) return i;
            else if (i < totalHrs && (time >= end && time < SharedPref.getInt(c, "hr" + (i + 1), "start")))
                return -1;
            else if (i == totalHrs && time > end) return 0;
        }
        return 0;
    }

    public static String getOrdinalStringFromInt(int i) {
        String[] sufixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + sufixes[i % 10];

        }
    }

    public static int determineLastPeriodNumber(Context c, long currentTimeMillis) {
        Date d = new Date(currentTimeMillis);
        int time = d.getHours() * 100 + d.getMinutes();
        int totalHrs = SharedPref.getInt(c, "totalHrs");
        for (int i = 1; i <= totalHrs; i++) {
            int start = SharedPref.getInt(c, "hr" + i, "start"),
                    end = SharedPref.getInt(c, "hr" + i, "end");
            if (time >= start && time <= end) return i;
            else if ((time >= end && time < SharedPref.getInt(c, "hr" + (i + 1), "start")))
                return i;
            else if (i == totalHrs && time > end) return 0;
        }
        return 0;
    }

    public static Notification prepareNotification(boolean goToActivity, String title, String message, Context context, Intent intent, SubjectObj obj) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, goToActivity ? intent : new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder nbuilder;
        NotificationManager notificationManager;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        Intent intentJoinFromPhone = new Intent(Intent.ACTION_VIEW, Uri.parse(obj.getZoomLink()));
        Intent intentJoinFromPC = new Intent(context, JoinMeetFromPCActivity.class).putExtra("subjDetails", new Gson().toJson(obj));

        PendingIntent piJoinFromPhone = PendingIntent.getActivity(context, 3, intentJoinFromPhone, PendingIntent.FLAG_ONE_SHOT);
        PendingIntent piJoinFromPC = PendingIntent.getActivity(context, 4, intentJoinFromPC, PendingIntent.FLAG_ONE_SHOT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder builder;
            notificationManager.createNotificationChannel(new NotificationChannel("1", "next period alert", NotificationManager.IMPORTANCE_HIGH));
            NotificationChannel channel = notificationManager.getNotificationChannel("1");
            channel.enableLights(true);
            channel.setLightColor(context.getResources().getColor(R.color.magenta));
            channel.setDescription("next period alert");
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build());

            builder = new Notification.Builder(context, "1")
                    .setContentTitle(title)
                    .setSmallIcon(R.drawable.ic_joinmeet)
                    .setStyle(new Notification.BigTextStyle().bigText(message))
                    .setCategory(Notification.CATEGORY_EVENT)
                    .setColorized(true)
                    .addAction(new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_join_from_phone), "Join From Phone", piJoinFromPhone).build())
                    .addAction(new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_join_from_computer), "Join From PC", piJoinFromPC).build())
                    .setColor(context.getResources().getColor(R.color.magenta))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            return builder.build();
        } else {
            nbuilder = new NotificationCompat.Builder(context, "1")
                    .setContentTitle(title)
                    .setSmallIcon(R.drawable.ic_joinmeet)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .addAction(R.drawable.ic_joinmeet, "Join From Mobile", piJoinFromPhone)
                    .addAction(R.drawable.ic_joinmeet, "Join From PC", piJoinFromPC)
                    .setAutoCancel(true)
                    .setPriority(NotificationManagerCompat.IMPORTANCE_MAX)
                    .setCategory(NotificationCompat.CATEGORY_EVENT)
                    .setColorized(true)
                    .setColor(context.getResources().getColor(R.color.colorAccent))
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setContentIntent(pendingIntent);
            return nbuilder.build();
        }
    }

    public static SubjectObj getSubjectObjFromSubCode(Context c, String subCode) {
        int ct = SharedPref.getInt(c, "subj_count");
        Gson gson = new Gson();

        for (int i = 1; i <= ct; i++) {
            SubjectObj obj = gson.fromJson(SharedPref.getString(c, String.valueOf(i)), SubjectObj.class);
            if (obj.getSubCode().equals(subCode)) return obj;
        }
        return null;
    }

    public static int getSubjectObjIndexFromSubCode(Context c, String subCode) {
        int ct = SharedPref.getInt(c, "subj_count");
        Gson gson = new Gson();

        for (int i = 1; i <= ct; i++) {
            SubjectObj obj = gson.fromJson(SharedPref.getString(c, String.valueOf(i)), SubjectObj.class);
            if (obj.getSubCode().equals(subCode)) return i;
        }
        return ct;
    }

    public static ArrayList<SubjectObj> getSubjectObjsList(Context c) {
        int ct = SharedPref.getInt(c, "subj_count");
        Gson gson = new Gson();

        ArrayList<SubjectObj> objs = new ArrayList<>();

        for (int i = 1; i <= ct; i++) {
            SubjectObj obj = gson.fromJson(SharedPref.getString(c, String.valueOf(i)), SubjectObj.class);
            objs.add(obj);
        }
        return objs;
    }

    public static Date getStartTimeFromHour(Context c, Integer hour) {
        Date d = new Date();
        d.setHours(SharedPref.getInt(c, "hr" + hour, "start") / 100);
        d.setMinutes(SharedPref.getInt(c, "hr" + hour, "start") % 100);
        return d;
    }

    public static boolean wifiIsConnected(Context c) {
        WifiManager wifiMgr = (WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            return wifiInfo.getNetworkId() == -1;
        }
        else return false; // Wi-Fi adapter is OFF
    }
}
