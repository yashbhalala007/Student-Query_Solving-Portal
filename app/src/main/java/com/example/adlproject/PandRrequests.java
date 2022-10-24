package com.example.adlproject;

public class PandRrequests {

    private String facultyName, Problem, date, studentName;

    public PandRrequests() {
    }

    public PandRrequests(String facultyName, String Problem, String date, String studentName) {
        this.facultyName = facultyName;
        this.Problem = Problem;
        this.date = date;
        this.studentName = studentName;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getProblem() {
        return Problem;
    }

    public void setProblem(String problem) {
        this.Problem = problem;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
