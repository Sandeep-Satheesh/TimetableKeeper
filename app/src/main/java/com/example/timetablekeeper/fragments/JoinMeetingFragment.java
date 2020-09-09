package com.example.timetablekeeper.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.example.timetablekeeper.R;
import com.example.timetablekeeper.adapters.JoinMeetAdapter;
import com.example.timetablekeeper.models.SubjectObj;
import com.example.timetablekeeper.utils.CommonUtils;
import com.example.timetablekeeper.utils.SharedPref;
import com.google.gson.Gson;

import java.util.ArrayList;

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
        SharedPref.putInt(getContext(), "totalHrs",5);
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
                new populateSubjectsList().execute();
            }
        });

        new populateSubjectsList().execute();
    }

    @Override
    public void onResume() {
        if (SharedPref.getBoolean(getContext(), "updateFlag1")) {
            SharedPref.putBoolean(getContext(), "updateFlag1", false);
            new populateSubjectsList().execute();
        }
        super.onResume();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    private class populateSubjectsList extends AsyncTask<Void, Void, Void> {
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
                 final int currentHr = adapter.getCurrentHour();

                if (currentHr > 0) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
                            tvCurrentHour.setText("It's now the " + CommonUtils.getOrdinalStringFromInt(currentHr) + " hour: " + currentSub.getSubShortForm().toUpperCase() + "!");
                            tvFacultyName.setText("Faculty: " + currentSub.getFacultyName());
                            tvNextClassHeader.setText("Next in line for you today:");
                            if (adapter.getNextSubject() == null) {
                                tvNextClassHeader.setText("No more classes scheduled for today!");
                                tvNextClassHeader.setVisibility(View.VISIBLE);
                                adapter.clear();
                                subRV.setVisibility(View.GONE);
                                linearLayout.setGravity(Gravity.CENTER);
                                return;
                            }
                            btnJoinCurrentClass.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(currentSub.getZoomLink())));
                                }
                            });
                        }
                    });
                } else if (currentHr == 0) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            linearLayout.setGravity(Gravity.CENTER_VERTICAL);
                            tvCurrentHour.setText("Yay! You don't have any more classes scheduled for today!");
                            btnJoinCurrentClass.setVisibility(View.GONE);
                            tvNextClassHeader.setVisibility(View.GONE);
                            tvFacultyName.setText("");
                            subRV.setVisibility(View.GONE);
                        }
                    });
                }  else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnJoinCurrentClass.setVisibility(View.GONE);
                            tvCurrentHour.setText("No classes going on currently!");
                            tvFacultyName.setText("Check your schedule for the day below.");
                        }
                    });
                }
            }
            else {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvCurrentHour.setText("Swipe right to set up your timetable and start using the app!");
                        btnJoinCurrentClass.setVisibility(View.GONE);
                        tvFacultyName.setText("");
                        tvNextClassHeader.setVisibility(View.GONE);
                        subRV.setVisibility(View.GONE);
                        linearLayout.setGravity(Gravity.CENTER);

                    }
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
                swipeRefreshLayout.setRefreshing(false);
                LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.rv_layoutanimation);
                subRV.setLayoutAnimation(controller);
                adapter.notifyDataSetChanged();
                subRV.scheduleLayoutAnimation();
            }
        }
    }
}
