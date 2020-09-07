package com.example.timetablekeeper.fragments;

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

import com.example.timetablekeeper.R;
import com.example.timetablekeeper.adapters.AddSubjectAdapter;
import com.example.timetablekeeper.adapters.JoinMeetAdapter;
import com.example.timetablekeeper.models.SubjectObj;
import com.example.timetablekeeper.utils.SharedPref;
import com.google.gson.Gson;

import java.util.ArrayList;

public class JoinMeetingFragment extends Fragment {
    JoinMeetAdapter adapter;
    RecyclerView subRV;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new populateSubjectsList().execute();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_addsub, container, false);
        initUI(v);
        return v;
    }

    private void initUI(View v) {
        subRV = v.findViewById(R.id.rv_subslist);
        subRV.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
            if (ct > 0) adapter = new JoinMeetAdapter(getContext(), objs);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (subRV != null && adapter != null) {
                subRV.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }
    }
}
