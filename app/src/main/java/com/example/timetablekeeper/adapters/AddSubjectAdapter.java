package com.example.timetablekeeper.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.example.timetablekeeper.AddSubjectActivity;
import com.example.timetablekeeper.R;
import com.example.timetablekeeper.models.SubjectObj;
import com.example.timetablekeeper.utils.CommonUtils;
import com.example.timetablekeeper.utils.SharedPref;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AddSubjectAdapter extends RecyclerView.Adapter<AddSubjectAdapter.SubjectsViewHolder> {
    ArrayList<SubjectObj> objs;
    Context c;
    TextDrawable.IBuilder builder;

    public AddSubjectAdapter(Context c, ArrayList<SubjectObj> subjectObjs) {
        objs = subjectObjs;
        Collections.sort(objs, new Comparator<SubjectObj>() {
            @Override
            public int compare(SubjectObj t2, SubjectObj t1) {
                return t2.getSubCode().compareTo(t1.getSubCode());
            }
        });
        this.c = c;
        builder = TextDrawable.builder()
                .beginConfig()
                .toUpperCase()
                .bold()
                .endConfig()
                .round();
    }

    @NonNull
    @Override
    public AddSubjectAdapter.SubjectsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SubjectsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.addsub_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AddSubjectAdapter.SubjectsViewHolder holder, int position) {
        SubjectObj obj = objs.get(position);
        holder.tvSubFullName.setText(String.format("%s - %s", obj.getSubCode(), obj.getSubName()));
        holder.tvFaculty.setText("Faculty: " + obj.getFacultyName());
        int col = ColorGenerator.MATERIAL.getColor(obj);
        TextDrawable d = builder.build(obj.getSubShortForm(), col);
        holder.ivSubShortForm.setImageDrawable(d);
    }

    @Override
    public int getItemCount() {
        return objs.size();
    }

    public class SubjectsViewHolder extends RecyclerView.ViewHolder {
        public TextView tvSubFullName, tvFaculty;
        public ImageView ivSubShortForm;
        public ImageButton btnEditSub, btnRemoveSub;

        public SubjectsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubFullName = itemView.findViewById(R.id.tv_subj_fullname);
            tvFaculty = itemView.findViewById(R.id.tv_facultyname);
            ivSubShortForm = itemView.findViewById(R.id.iv_subshortform);
            btnEditSub = itemView.findViewById(R.id.ib_editsub);
            btnRemoveSub = itemView.findViewById(R.id.ib_remove_sub);

            btnEditSub.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    c.startActivity(new Intent(c, AddSubjectActivity.class).putExtra("subCode", tvSubFullName.getText().toString().substring(0, tvSubFullName.getText().toString().indexOf(' '))));
                }
            });
            btnRemoveSub.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(c)
                            .setTitle("Delete subject forever?")
                            .setMessage("You won't be able to undo this action!")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int x) {
                                    int ct = SharedPref.getInt(c, "subj_count");
                                    String k = tvSubFullName.getText().toString().substring(0, tvSubFullName.getText().toString().indexOf(' '));
                                    int pos = CommonUtils.getSubjectObjIndexFromSubCode(c, k);
                                    if (objs.size() > 1) {
                                        objs.remove(pos-1);
                                        notifyItemRemoved(pos);
                                    }
                                    else {
                                        objs.clear();
                                        notifyDataSetChanged();
                                    }
                                    SharedPref.putInt(c, "subj_count", ct - 1);
                                    SharedPref.putBoolean(c, "updateFlag1", true);
                                    SharedPref.putBoolean(c, "updateFlag2", true);
                                    SharedPref.putBoolean(c, "scheduleUpdatedFlg", false);
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            });
        }
    }
}
