package com.example.adlproject;

public class User {
    public String name, email, userName, userRole, gender, department, enrolment_no;

    public User() {

    }

    public User(String name, String email, String user, String role, String gender, String depart, String en_no) {
        this.name = name;
        this.email = email;
        this.userName = user;
        this.userRole = role;
        this.gender = gender;
        this.department = depart;
        this.enrolment_no = en_no;
    }
}
