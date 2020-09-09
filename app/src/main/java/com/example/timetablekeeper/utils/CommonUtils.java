package com.example.timetablekeeper.utils;

import android.content.Context;

import com.example.timetablekeeper.models.SubjectObj;
import com.google.gson.Gson;

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
            else if (i < totalHrs && (time >= end && time < SharedPref.getInt(c, "hr" + (i + 1), "start"))) return -1;
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
            else if ((time >= end && time < SharedPref.getInt(c, "hr" + (i + 1), "start"))) return i;
            else if (i == totalHrs && time > end) return 0;
        }
        return 0;
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
}
