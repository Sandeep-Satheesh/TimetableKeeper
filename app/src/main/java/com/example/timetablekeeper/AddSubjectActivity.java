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

import com.example.timetablekeeper.models.SubjectObj;
import com.example.timetablekeeper.utils.SharedPref;
import com.github.informramiz.daypickerlibrary.views.DayPickerDialog;
import com.github.informramiz.daypickerlibrary.views.DayPickerView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class AddSubjectActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Add New Subject");
        setContentView(R.layout.activity_addsub);
        initUI();
    }

    private void initUI() {
        TextView btnCancel = findViewById(R.id.cmd_cancel),
                btnOK = findViewById(R.id.cmd_addsub);
        final TextInputEditText etSubCode = findViewById(R.id.et_subcode),
                etSubName = findViewById(R.id.et_subname),
                etSubShortForm = findViewById(R.id.et_subshortform),
                etFacultyName = findViewById(R.id.et_facultyname),
                etZoomLink = findViewById(R.id.et_zoomlink);

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etZoomLink.getText() == null || etSubCode.getText() == null || etSubName.getText() == null || etFacultyName.getText() == null || etSubShortForm.getText() == null) {
                    Toast.makeText(getApplicationContext(), "There are one or more missing fields! Please fill them up to continue!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (etSubCode.getText().length() > 7 || !etSubCode.getText().toString().matches("[A-ZA-ZA-ZA-ZA-Z][0-90-90-90-9]")) {
                    Toast.makeText(getApplicationContext(), "Invalid subject code! Please re-enter!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (etSubShortForm.getText().length() > 3) {
                    Toast.makeText(getApplicationContext(), "Please choose a short form having less than 5 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!Patterns.WEB_URL.matcher(etZoomLink.getText().toString()).matches()) {
                    Toast.makeText(getApplicationContext(), "Please enter a valid link!", Toast.LENGTH_SHORT).show();
                    return;
                }
                final HashMap<Integer, String> timings = new HashMap<>();
                new DayPickerDialog.Builder(AddSubjectActivity.this)
                        .setMultiSelectionAllowed(true)
                        .setThemeResId(R.style.DayPickerTheme)
                        .setOnDaysSelectedListener(new DayPickerDialog.OnDaysSelectedListener() {
                            @Override
                            public void onDaysSelected(DayPickerView dayPickerView, final boolean[] selectedDays) {
                                if (selectedDays.length == 0) {
                                    Toast.makeText(getApplicationContext(), "Please choose at least one day!", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                else if (selectedDays[0]) {
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
                                    new AlertDialog.Builder(AddSubjectActivity.this)
                                            .setTitle("Choose hour(s) on " + days[i] + ":")
                                            .setMultiChoiceItems(new CharSequence[]{"1st Hour", "2nd Hour", "3rd Hour", "4th Hour", "5th Hour"}, null, new DialogInterface.OnMultiChoiceClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                                    if (b) a.add(String.valueOf(i+1));
                                                    else a.remove(String.valueOf(i+1));
                                                }
                                            })
                                            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int j) {
                                                    StringBuilder s = new StringBuilder();
                                                    for (int x = 0; x < a.size(); x++)
                                                        s.append(a.get(x));
                                                    timings.put((Integer) finalI, s.toString());
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
                                            new AlertDialog.Builder(AddSubjectActivity.this)
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
                                    }).show();
                                }
                            }
                        }).build().show();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void insertSubjectIntoSharedPref(String subCode, String subName, String subShortForm, String facultyName, HashMap<Integer, String> timings, String zoomLink) {
        int subjCount = SharedPref.getInt(getApplicationContext(), "subj_count");
        SharedPref.putInt(getApplicationContext(), "subj_count", subjCount + 1);
        SharedPref.putBoolean(getApplicationContext(), "updateFlag", true);
        SubjectObj obj = new SubjectObj(
                facultyName,
                subName,
                subCode,
                subShortForm,
                timings,
                zoomLink
        );
        Gson gson = new Gson();
        String json = gson.toJson(obj);
        SharedPref.putString(getApplicationContext(), String.valueOf(subjCount + 1), json);
    }
}
