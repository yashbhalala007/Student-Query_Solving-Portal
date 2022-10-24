package com.example.adlproject;

public class EventsList {

    String uid, date, time, eventDate, city, description, postImage, name, profileImage, status, enrolment_no;

    public EventsList() {

    }

    public EventsList(String uid, String date, String time, String eventDate, String city, String description, String postImage, String name, String profileImage, String status, String enrolment_no) {
        this.uid = uid;
        this.date = date;
        this.time = time;
        this.eventDate = eventDate;
        this.city = city;
        this.description = description;
        this.postImage = postImage;
        this.name = name;
        this.profileImage = profileImage;
        this.status = status;
        this.enrolment_no = enrolment_no;
    }

    public String getEnrolment_no() {
        return enrolment_no;
    }

    public void setEnrolment_no(String enrolment_no) {
        this.enrolment_no = enrolment_no;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
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

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
