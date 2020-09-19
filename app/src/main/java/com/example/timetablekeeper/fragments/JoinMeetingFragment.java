package com.example.timetablekeeper.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.example.timetablekeeper.JoinMeetFromPCActivity;
import com.example.timetablekeeper.utils.NotificationReminderBroadcastReceiver;
import com.example.timetablekeeper.R;
import com.example.timetablekeeper.adapters.JoinMeetAdapter;
import com.example.timetablekeeper.models.SubjectObj;
import com.example.timetablekeeper.utils.CommonUtils;
import com.example.timetablekeeper.utils.SharedPref;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;

public class JoinMeetingFragment extends Fragment {
    JoinMeetAdapter adapter;
    RecyclerView subRV;
    TextView tvCurrentHour, tvFacultyName, tvNextClassHeader;
    Button btnJoinCurrentClass;
    LinearLayout linearLayout;
    SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_joinmeet, container, false);
        initUI(v);

        SharedPref.putInt(getContext(), "totalHrs", 5);
        SharedPref.putInt(getContext(), "hr1", "start", 830);
        SharedPref.putInt(getContext(), "hr1", "end", 930);
        SharedPref.putInt(getContext(), "hr2", "start", 945);
        SharedPref.putInt(getContext(), "hr2", "end", 1045);
        SharedPref.putInt(getContext(), "hr3", "start", 1100);
        SharedPref.putInt(getContext(), "hr3", "end", 1200);
        SharedPref.putInt(getContext(), "hr4", "start", 1300);
        SharedPref.putInt(getContext(), "hr4", "end", 1400);
        SharedPref.putInt(getContext(), "hr5", "start", 1415);
        SharedPref.putInt(getContext(), "hr5", "end", 1515);
        SharedPref.putString(getContext(), "hostName", "192.168.1.4");
        SharedPref.putInt(getContext(), "portNumber", 6868);

        SharedPref.putBoolean(getContext(), "scheduleUpdatedFlg", true);
        return v;
    }

    private void initUI(View v) {
        subRV = v.findViewById(R.id.rv_subslist);
        subRV.setLayoutManager(new LinearLayoutManager(getContext()));
        tvCurrentHour = v.findViewById(R.id.tv_hourname);
        tvFacultyName = v.findViewById(R.id.tv_facultyname);
        btnJoinCurrentClass = v.findViewById(R.id.btn_joinmeet);
        tvNextClassHeader = v.findViewById(R.id.tv_nextclassname);
        linearLayout = v.findViewById(R.id.ll_joinmeet);

        swipeRefreshLayout = v.findViewById(R.id.swiperefresh_joinmeet);
        swipeRefreshLayout.setColorSchemeColors(ColorGenerator.DEFAULT.getRandomColor(), ColorGenerator.DEFAULT.getRandomColor(), ColorGenerator.DEFAULT.getRandomColor());
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new populateSchedule().execute();
            }
        });

        new populateSchedule().execute();
    }

    @Override
    public void onResume() {
        if (SharedPref.getBoolean(getContext(), "updateFlag1")) {
            SharedPref.putBoolean(getContext(), "updateFlag1", false);
            new populateSchedule().execute();
        }
        super.onResume();
    }

    private void updateTimetableAlarm() {
        if (getContext() == null) {
            Log.e("SubjectsFragment", "ERROR: COULD NOT SET ALARM, CONTEXT IS NULL!");
            return;
        }

        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        if (adapter == null || adapter.getCurrentHour() == 0 || adapter.getItemCount() == 0) {
            //Toast.makeText(getContext(), "No classes to schedule in advance!", Toast.LENGTH_SHORT).show();
            return;
        }

        //schedule the next alarm (notification).

        ArrayList<SubjectObj> objs = adapter.getObjs();
        ArrayList<Integer> hours = adapter.getHours();

        if (objs == null || objs.size() == 0) return;

        Date startTime = CommonUtils.getStartTimeFromHour(getContext(), hours.get(0));
        SubjectObj obj = objs.get(0);
        //startActivity(new Intent(getContext(), JoinMeetFromPCActivity.class).putExtra("subjDetails", new Gson().toJson(obj)));
        Intent intent = new Intent(getContext(), NotificationReminderBroadcastReceiver.class).putExtra("subjDetails", new Gson().toJson(obj));
        PendingIntent notificationPendingIntent = PendingIntent.getBroadcast(getContext(), 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTime.getTime(), notificationPendingIntent);
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, startTime.getTime(), notificationPendingIntent);

        objs.remove(0);
        hours.remove(0);

        if (objs.size() > 0) {
            String s = new Gson().toJson(objs);
            String s1 = new Gson().toJson(hours);
            SharedPref.putString(getContext(), "remainingSubjs", s);
            SharedPref.putString(getContext(), "remainingHrs", s1);
        }
        SharedPref.putBoolean(getContext(), "scheduleUpdatedFlg", false);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private class populateSchedule extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setColorSchemeColors(ColorGenerator.DEFAULT.getRandomColor(), ColorGenerator.DEFAULT.getRandomColor(), ColorGenerator.DEFAULT.getRandomColor());
            subRV.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Gson gson = new Gson();
            ArrayList<SubjectObj> objs = new ArrayList<>();
            int ct = SharedPref.getInt(getContext(), "subj_count");
            for (int i = 1; i <= ct; i++) {
                String idx = String.valueOf(i);
                SubjectObj obj = gson.fromJson(SharedPref.getString(getContext(), idx), SubjectObj.class);
                objs.add(obj);
            }
            if (ct > 0) {
                adapter = new JoinMeetAdapter(getContext(), objs);
                final SubjectObj currentSub = adapter.getCurrentSub();
                Date d = new Date();
                int currentHr;
                if (d.getDay() == 0) {
                    adapter = null;
                    currentHr = 0;
                } else currentHr = adapter.getCurrentHour();
                if (currentHr > 0) {
                    final int finalCurrentHr = currentHr;
                    getActivity().runOnUiThread(() -> {
                        if (currentSub == null) {
                            btnJoinCurrentClass.setVisibility(View.GONE);
                            tvCurrentHour.setText("No classes going on currently!");
                            tvFacultyName.setText("");
                            return;
                        }
                        linearLayout.setGravity(Gravity.NO_GRAVITY);
                        tvNextClassHeader.setVisibility(View.VISIBLE);
                        subRV.setVisibility(View.VISIBLE);
                        btnJoinCurrentClass.setVisibility(View.VISIBLE);
                        btnJoinCurrentClass.setEnabled(true);
                        tvCurrentHour.setText("It's now the " + CommonUtils.getOrdinalStringFromInt(finalCurrentHr) + " hour: " + currentSub.getSubShortForm().toUpperCase() + "!");
                        tvFacultyName.setText("Faculty: " + currentSub.getFacultyName());
                        tvNextClassHeader.setText("Next in line for you today:");
                        if (adapter.getNextSubject() == null) {
                            tvNextClassHeader.setText("No more classes scheduled for today!");
                            tvNextClassHeader.setVisibility(View.VISIBLE);
                            adapter.clear();
                            adapter.notifyDataSetChanged();
                            subRV.setVisibility(View.GONE);
                            linearLayout.setGravity(Gravity.CENTER);
                        }
                        btnJoinCurrentClass.setOnClickListener(view -> {
                            //start activity to choose whether desktop or mobile
                            btnJoinCurrentClass.setEnabled(false);
                            if (CommonUtils.wifiIsConnected(getContext())) {
                                startActivity(new Intent(getContext(), JoinMeetFromPCActivity.class).putExtra("subjDetails", new Gson().toJson(currentSub)));
                            }
                            else new AlertDialog.Builder(getContext())
                                    .setTitle("No network connection")
                                    .setMessage("You're not connected to a Wi-Fi network! Please connect to the same Wi-Fi network as your computer to send the message to it, and press 'Retry'. If you'd like to cancel the sending operation, please press 'Cancel'.")
                                    .setPositiveButton("Retry", (dialogInterface, i) -> {
                                        if (CommonUtils.wifiIsConnected(getContext())) {
                                            dialogInterface.cancel();
                                            startActivity(new Intent(getContext(), JoinMeetFromPCActivity.class).putExtra("subjDetails", new Gson().toJson(currentSub)));
                                        }
                                    })
                                    .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel())
                                    .setCancelable(false)
                                    .show();
                            btnJoinCurrentClass.setEnabled(true);
                        });
                    });
                } else if (currentHr == 0) {
                    getActivity().runOnUiThread(() -> {
                        linearLayout.setGravity(Gravity.CENTER_VERTICAL);
                        tvCurrentHour.setText("Yay! You don't have any more classes scheduled for today!");
                        btnJoinCurrentClass.setVisibility(View.GONE);
                        tvNextClassHeader.setVisibility(View.GONE);
                        tvFacultyName.setText("");
                        if (adapter != null) {
                            adapter.clear();
                            adapter.notifyDataSetChanged();
                        }
                        subRV.setVisibility(View.GONE);
                    });
                } else {
                    getActivity().runOnUiThread(() -> {
                        btnJoinCurrentClass.setVisibility(View.GONE);
                        tvCurrentHour.setText("No classes going on currently");
                        tvFacultyName.setText("Enjoy your break!.");
                    });
                }
            } else {
                getActivity().runOnUiThread(() -> {
                    tvCurrentHour.setText("Swipe right to set up your timetable and start using the app!");
                    btnJoinCurrentClass.setVisibility(View.GONE);
                    tvFacultyName.setText("");
                    if (adapter != null) {
                        adapter.clear();
                        adapter.notifyDataSetChanged();
                    }
                    tvNextClassHeader.setVisibility(View.GONE);
                    subRV.setVisibility(View.GONE);
                    linearLayout.setGravity(Gravity.CENTER);

                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (subRV != null && adapter != null) {
                subRV.setVisibility(View.VISIBLE);
                subRV.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.rv_layoutanimation);
                subRV.setLayoutAnimation(controller);
                adapter.notifyDataSetChanged();
                subRV.scheduleLayoutAnimation();
            }
            if (SharedPref.getBoolean(getContext(), "scheduleUpdatedFlg"))
                updateTimetableAlarm();
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
