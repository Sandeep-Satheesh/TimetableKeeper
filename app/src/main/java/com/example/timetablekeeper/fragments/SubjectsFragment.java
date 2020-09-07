package com.example.timetablekeeper.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_addsub, container, false);
        initUI(v);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new populateSubjectsList().execute();
    }

    private void initUI(View v) {
        FloatingActionButton b = v.findViewById(R.id.floatingActionButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(view.getContext(), AddSubjectActivity.class), 1);
            }
        });
        subRV = v.findViewById(R.id.rv_subslist);
        subRV.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onResume() {
        if (SharedPref.getBoolean(getContext(), "updateFlag")) {
            SharedPref.putBoolean(getContext(), "updateFlag", false);
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
                subRVAdapter.notifyDataSetChanged();
            }
        }
    }
}
