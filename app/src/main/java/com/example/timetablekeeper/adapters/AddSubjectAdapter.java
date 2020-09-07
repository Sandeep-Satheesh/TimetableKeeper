package com.example.timetablekeeper.adapters;

import android.content.Context;
import android.content.DialogInterface;
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
import com.example.timetablekeeper.R;
import com.example.timetablekeeper.models.SubjectObj;
import com.example.timetablekeeper.utils.SharedPref;
import com.google.gson.Gson;

import java.util.ArrayList;

public class AddSubjectAdapter extends RecyclerView.Adapter<AddSubjectAdapter.SubjectsViewHolder> {
    ArrayList<SubjectObj> objs;
    Context c;
    TextDrawable.IBuilder builder;

    public AddSubjectAdapter(Context c, ArrayList<SubjectObj> subjectObjs) {
        objs = subjectObjs;
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
                                    int pos = -1;
                                    String k = tvSubFullName.getText().toString().substring(0, tvSubFullName.getText().toString().indexOf(' '));
                                    for (int i = 1; i <= ct; i++) {
                                        String idx = String.valueOf(i);
                                        SubjectObj obj = new Gson().fromJson(SharedPref.getString(c, idx), SubjectObj.class);
                                        if (obj.getSubCode().equals(k)){
                                            pos = i;
                                            break;
                                        }
                                    }
                                    objs.remove(pos-1);
                                    notifyItemRemoved(pos-1);
                                    SharedPref.putInt(c, "subj_count", ct - 1);
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            });
        }
    }
}
