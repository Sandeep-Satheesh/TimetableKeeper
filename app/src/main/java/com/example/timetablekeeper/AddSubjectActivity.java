package com.example.timetablekeeper;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;

import com.example.timetablekeeper.models.SubjectObj;
import com.example.timetablekeeper.utils.CommonUtils;
import com.example.timetablekeeper.utils.SharedPref;
import com.github.informramiz.daypickerlibrary.views.DayPickerDialog;
import com.github.informramiz.daypickerlibrary.views.DayPickerView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class AddSubjectActivity extends AppCompatActivity {
    TextInputEditText etSubCode, etSubName, etSubShortForm, etFacultyName, etZoomLink;
    TextView btnCancel, btnOK, btnSaveWithoutChangingTT;
    AppCompatCheckBox cbPracticalSubj;
    volatile boolean scheduleUpdated = false;
    boolean[] days;
    SubjectObj obj;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getStringExtra("subCode") != null)
            setTitle("Edit Subject Details:");
        else
            setTitle("Enter New Subject Details:");
        setContentView(R.layout.activity_addsub);
        initUI();
        if (getIntent().getStringExtra("subCode") != null) {
            String subCode = getIntent().getStringExtra("subCode");
            obj = CommonUtils.getSubjectObjFromSubCode(getApplicationContext(), subCode);
            etSubCode.setText(obj.getSubCode());
            etFacultyName.setText(obj.getFacultyName());
            etSubName.setText(obj.getSubName());
            etZoomLink.setText(obj.getZoomLink());
            etSubShortForm.setText(obj.getSubShortForm());
            etSubCode.setEnabled(false);
            btnOK.setText("Edit Timetable");
        }
        else btnSaveWithoutChangingTT.setVisibility(View.GONE);
    }

    private void initUI() {
        btnCancel = findViewById(R.id.cmd_cancel);
        btnOK = findViewById(R.id.cmd_addsub);
        etSubCode = findViewById(R.id.et_subcode);
        etSubName = findViewById(R.id.et_subname);
        btnSaveWithoutChangingTT = findViewById(R.id.cmd_savewithout_tt_change);
        etSubShortForm = findViewById(R.id.et_subshortform);
        etFacultyName = findViewById(R.id.et_facultyname);
        etZoomLink = findViewById(R.id.et_zoomlink);
        cbPracticalSubj = findViewById(R.id.cb_islabsubj);
        cbPracticalSubj.setVisibility(View.GONE); //TODO: Implement support for lab hours later.

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etZoomLink.getText() == null || etSubCode.getText() == null || etSubName.getText() == null || etFacultyName.getText() == null || etSubShortForm.getText() == null) {
                    Toast.makeText(getApplicationContext(), "There are one or more missing fields! Please fill them up to continue!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (etZoomLink.getText().toString().isEmpty() || etSubCode.getText().toString().isEmpty() || etSubName.getText().toString().isEmpty() || etFacultyName.getText().toString().isEmpty() || etSubShortForm.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "There are one or more missing fields! Please fill them up to continue!", Toast.LENGTH_SHORT).show();
                    return;
                }

                etSubCode.setText(etSubCode.getText().toString().trim().toUpperCase());
                etFacultyName.setText(etFacultyName.getText().toString().trim());
                etSubName.setText(etSubName.getText().toString().trim());
                etSubShortForm.setText(etSubShortForm.getText().toString().trim().toUpperCase());
                etZoomLink.setText(etZoomLink.getText().toString().trim());

                /*if (etSubCode.getText().length() > 7 || !etSubCode.getText().toString().matches("[A-Z]+[0-9]+")) {
                    Toast.makeText(getApplicationContext(), "Invalid subject code! Please re-enter!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (etSubShortForm.getText().length() > 3) {
                    Toast.makeText(getApplicationContext(), "Please choose a short form having less than 3 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!Patterns.WEB_URL.matcher(etZoomLink.getText().toString()).matches()) {
                    Toast.makeText(getApplicationContext(), "Please enter a valid link!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (getIntent().getStringExtra("subCode") == null && CommonUtils.getSubjectObjFromSubCode(getApplicationContext(), etSubCode.getText().toString()) != null) {
                    etSubCode.setError("There's already a subject with the same subject code!");
                    return;
                }*/
                if (view.getId() == R.id.cmd_addsub) setTimetable();
                else {
                    insertSubjectIntoSharedPref(etSubCode.getText().toString(),
                            etSubName.getText().toString(),
                            etSubShortForm.getText().toString(),
                            etFacultyName.getText().toString(),
                            obj.getTimetable(),
                            etZoomLink.getText().toString());
                    finish();
                }
            }
        };
        btnSaveWithoutChangingTT.setOnClickListener(listener);
        btnOK.setOnClickListener(listener);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void setTimetable() {
        days = new boolean[7];
        if (obj != null) {
            for (int i = 0; i < 7; i++) {
                if (obj.getTimetable().containsKey(i)) days[i] = true;
            }
        }
        final HashMap<Integer, String> timings = new HashMap<>();
        DayPickerDialog d = new DayPickerDialog.Builder(AddSubjectActivity.this)
                .setMultiSelectionAllowed(true)
                .setInitialSelectedDays(days)
                .setThemeResId(R.style.DayPickerTheme)
                .setOnDaysSelectedListener(new DayPickerDialog.OnDaysSelectedListener() {
                    @Override
                    public void onDaysSelected(DayPickerView dayPickerView, final boolean[] selectedDays) {
                        if (selectedDays.length == 0) {
                            Toast.makeText(getApplicationContext(), "Please choose at least one day!", Toast.LENGTH_SHORT).show();
                            return;
                        } else if (selectedDays[0]) {
                            Toast.makeText(getApplicationContext(), "No classes on Sundays!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int ct = 0;
                        for (boolean selectedDay : selectedDays) if (selectedDay) ct++;

                        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
                        for (int i = selectedDays.length - 1; i > 0; i--) {
                            if (!selectedDays[i]) continue;
                            final ArrayList<String> a = new ArrayList<>();
                            final int finalCt = ct;
                            final int finalI = i;
                            boolean[] selectedHrs = null;
                            int totalHrs = SharedPref.getInt(getApplicationContext(), "totalHrs");
                            if (obj != null) {
                                selectedHrs = new boolean[totalHrs];
                                String s = obj.getTimetable().get(i);
                                if (s != null) {
                                    for (int h = 1; h <= totalHrs; h++) {
                                        if (s.contains(String.valueOf(h)))
                                            selectedHrs[h - 1] = true;
                                    }
                                }
                            }
                            CharSequence[] hoursList = new CharSequence[totalHrs];
                            for (int j = 0; j < hoursList.length; j++) {
                                hoursList[j] = CommonUtils.getOrdinalStringFromInt(j+1) + " Hour";
                            }
                            new AlertDialog.Builder(AddSubjectActivity.this, R.style.DayPickerTheme)
                                    .setTitle("Choose hour(s) on " + days[i] + ":")
                                    .setMultiChoiceItems(hoursList, selectedHrs, new DialogInterface.OnMultiChoiceClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                            if (b) a.add(String.valueOf(i + 1));
                                            else a.remove(String.valueOf(i + 1));
                                        }
                                    })
                                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int j) {
                                            StringBuilder s = new StringBuilder();
                                            for (int x = 0; x < a.size(); x++)
                                                s.append(a.get(x));
                                            timings.put((Integer) finalI, s.toString());
                                            scheduleUpdated = true;
                                            if (timings.keySet().size() == finalCt) {
                                                insertSubjectIntoSharedPref(
                                                        etSubCode.getText().toString(),
                                                        etSubName.getText().toString(),
                                                        etSubShortForm.getText().toString(),
                                                        etFacultyName.getText().toString(),
                                                        timings,
                                                        etZoomLink.getText().toString()
                                                );
                                                finish();
                                            }
                                        }
                                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    new AlertDialog.Builder(AddSubjectActivity.this, R.style.DayPickerTheme)
                                            .setTitle("Cancel adding subject?")
                                            .setMessage("You'll have to repeat this process again next time!")
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                    finish();
                                                }
                                            }).setNegativeButton("No", null).show();
                                }
                            }).setCancelable(false).show();
                        }
                    }
                }).build();
        d.setCancelable(false);
        d.show();
    }

    private void insertSubjectIntoSharedPref(String subCode, String subName, String subShortForm, String facultyName, HashMap<Integer, String> timings, String zoomLink) {
        int subIdx;
        if (obj == null) {
            subIdx = SharedPref.getInt(getApplicationContext(), "subj_count") + 1;
            SharedPref.putInt(getApplicationContext(), "subj_count", subIdx);
        } else
            subIdx = CommonUtils.getSubjectObjIndexFromSubCode(getApplicationContext(), obj.getSubCode());

        SubjectObj obj = new SubjectObj(
                facultyName,
                subName,
                subCode,
                subShortForm,
                timings,
                zoomLink
        );
        SharedPref.putString(getApplicationContext(), String.valueOf(subIdx), new Gson().toJson(obj));
        SharedPref.putBoolean(getApplicationContext(), "updateFlag1", true);
        SharedPref.putBoolean(getApplicationContext(), "updateFlag2", true);
        SharedPref.putBoolean(getApplicationContext(), "scheduleUpdatedFlg", scheduleUpdated);
    }
}
