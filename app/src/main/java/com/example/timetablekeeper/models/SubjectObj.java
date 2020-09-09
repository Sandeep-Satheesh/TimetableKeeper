package com.example.timetablekeeper.models;

import java.util.HashMap;

public class SubjectObj {
    String facultyName;
    String subName;
    String subCode;
    String subShortForm;
    String zoomLink;

    public boolean isLabSubj() {
        return isLabSubj;
    }

    public void setLabSubj(boolean labSubj) {
        isLabSubj = labSubj;
    }

    boolean isLabSubj;

    public String getZoomLink() {
        return zoomLink;
    }

    public void setZoomLink(String zoomLink) {
        this.zoomLink = zoomLink;
    }

    HashMap<Integer, String> timetable;

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getSubName() {
        return subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }

    public String getSubCode() {
        return subCode;
    }

    public void setSubCode(String subCode) {
        this.subCode = subCode;
    }

    public String getSubShortForm() {
        return subShortForm;
    }

    public void setSubShortForm(String subShortForm) {
        this.subShortForm = subShortForm;
    }

    public HashMap<Integer, String> getTimetable() {
        return timetable;
    }

    public void setTimetable(HashMap<Integer, String> timetable) {
        this.timetable = timetable;
    }

    public SubjectObj(String facultyName, String subName, String subCode, String subShortForm, HashMap<Integer, String> timetable, String zoomLink) {
        this.facultyName = facultyName;
        this.subName = subName;
        this.subCode = subCode;
        this.subShortForm = subShortForm;
        this.timetable = timetable;
        this.zoomLink = zoomLink;
    }
}
