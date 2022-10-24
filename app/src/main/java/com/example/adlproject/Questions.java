package com.example.adlproject;

public class Questions {
    String uid, date, time, question, Image, answer, enrolment_no, status;

    public Questions() {
    }

    public Questions(String uid, String date, String time, String question, String Image, String answer, String enrolment_no, String status) {
        this.uid = uid;
        this.date = date;
        this.time = time;
        this.question = question;
        this.Image = Image;
        this.answer = answer;
        this.enrolment_no = enrolment_no;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEnrolment_no() {
        return enrolment_no;
    }

    public void setEnrolment_no(String enrolment_no) {
        this.enrolment_no = enrolment_no;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String Image) {
        this.Image = Image;
    }
}
