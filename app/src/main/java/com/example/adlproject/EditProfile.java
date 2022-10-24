package com.example.adlproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditProfile extends AppCompatActivity {
    private TextInputLayout name, en, user;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    String userid;
    Button btn;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        name = findViewById(R.id.ilname);
        en = findViewById(R.id.il_enrol);
        user = findViewById(R.id.il_username);
        btn = findViewById(R.id.submit);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);
        mAuth = FirebaseAuth.getInstance();
        userid = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child("Users").child(userid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        progressBar.setVisibility(View.VISIBLE);
                        dataSnapshot.getRef().child("userName").setValue(user.getEditText().getText().toString().trim());
                        dataSnapshot.getRef().child("name").setValue(name.getEditText().getText().toString().trim());
                        dataSnapshot.getRef().child("enrolment_no").setValue(en.getEditText().getText().toString().trim());
                        Intent intent = new Intent(EditProfile.this, Profile.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDatabase.child("Users").child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name.getEditText().setText((String) dataSnapshot.child("name").getValue());
                user.getEditText().setText((String) dataSnapshot.child("userName").getValue());
                en.getEditText().setText((String) dataSnapshot.child("enrolment_no").getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
