package com.example.timetablekeeper.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.example.timetablekeeper.AddSubjectActivity;
import com.example.timetablekeeper.R;
import com.example.timetablekeeper.adapters.AddSubjectAdapter;
import com.example.timetablekeeper.models.SubjectObj;
import com.example.timetablekeeper.utils.SharedPref;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.ArrayList;

public class SubjectsFragment extends Fragment {
    RecyclerView subRV;
    volatile AddSubjectAdapter subRVAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    boolean viewedOnce = false;
    FloatingActionButton fabAddSubj;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_addsub, container, false);
        initUI(v);
        return v;
    }

    private void initUI(View v) {
        fabAddSubj = v.findViewById(R.id.floatingActionButton);
        fabAddSubj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(view.getContext(), AddSubjectActivity.class), 1);
            }
        });
        subRV = v.findViewById(R.id.rv_subslist);
        subRV.setLayoutManager(new LinearLayoutManager(getContext()));
        subRV.setVisibility(View.INVISIBLE);

        swipeRefreshLayout = v.findViewById(R.id.swiperefresh_addsub);
        swipeRefreshLayout.setColorSchemeColors(ColorGenerator.DEFAULT.getRandomColor(), ColorGenerator.DEFAULT.getRandomColor(), ColorGenerator.DEFAULT.getRandomColor());
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new populateSubjectsList().execute();
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && !viewedOnce) {
            viewedOnce = true;
            subRV.setVisibility(View.VISIBLE);
            LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.rv_layoutanimation);
            subRV.setLayoutAnimation(controller);
            subRVAdapter.notifyDataSetChanged();
            subRV.scheduleLayoutAnimation();
        }
    }

    @Override
    public void onResume() {
        if (!viewedOnce || SharedPref.getBoolean(getContext(), "updateFlag2")) {
            SharedPref.putBoolean(getContext(), "updateFlag2", false);
            new populateSubjectsList().execute();
        }
        super.onResume();
    }

    private class populateSubjectsList extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
            if (ct > 0) subRVAdapter = new AddSubjectAdapter(getContext(), objs);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (subRV != null && subRVAdapter != null) {
                subRV.setAdapter(subRVAdapter);
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }
}
