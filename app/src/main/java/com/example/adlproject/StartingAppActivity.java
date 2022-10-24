package com.example.adlproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StartingAppActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 5000;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting_app);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        final FirebaseUser user =mAuth.getCurrentUser();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (user != null){
                    userid = mAuth.getCurrentUser().getUid();
                    mDatabase = FirebaseDatabase.getInstance().getReference();
                    mDatabase.child("Users").child(userid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String userRole = (String) dataSnapshot.child("userRole").getValue();
                            if (userRole.equals("Admin")) {
                                Intent intent = new Intent(getApplicationContext(), AdminDashboard.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(getApplicationContext(), Dashboard.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        }, SPLASH_TIME_OUT);
    }
}
