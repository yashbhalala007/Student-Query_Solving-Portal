package com.example.adlproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AdminDashboard extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button btn1, btn2, btn3, btn4, btn5, btn6, btn7;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference profileImage;
    private String userid, downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isConnected(getApplicationContext())){
            setContentView(R.layout.no_internet_connection);
            Log.i("MainActivity", "No Internet");
        }else {
            setContentView(R.layout.activity_admin_dashboard);

            btn1 = findViewById(R.id.admin_add_user);
            btn2 = findViewById(R.id.admin_profile);
            btn3 = findViewById(R.id.admin_question);
            btn4 = findViewById(R.id.admin_meetings);
            btn5 = findViewById(R.id.admin_events);
            btn6 = findViewById(R.id.admin_photos);
            btn7 = findViewById(R.id.admin_logout);

            mToolbar = findViewById(R.id.admin_mainpage_toolbar);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setTitle("Home");

            mAuth = FirebaseAuth.getInstance();
            userid = mAuth.getCurrentUser().getUid();
            mDatabase = FirebaseDatabase.getInstance().getReference();
            profileImage = FirebaseStorage.getInstance().getReference();
            final StorageReference filePath = profileImage.child("Default Profile Image").child("profile.png");
            filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    downloadUrl = task.getResult().toString();
                }
            });

            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = getPackageManager().getLaunchIntentForPackage("com.example.test");
                    if (intent != null) {
                        // We found the activity now start the activity
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("downloadURL", downloadUrl);
                        startActivity(intent);
                    } else {
                        // Bring user to the market or let them choose an app?
                        intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.parse("market://details?id=" + "com.example.test"));
                        startActivity(intent);
                    }
                }
            });

            btn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent profileIntent = new Intent(AdminDashboard.this, Profile.class);
                    profileIntent.putExtra("userId", userid);
                    profileIntent.putExtra("role", "Admin");
                    startActivity(profileIntent);
                }
            });

            btn3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AdminDashboard.this, AdminQuestions.class));
                }
            });

            btn4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startActivity(new Intent(AdminDashboard.this, AdminMeetings.class));
                }
            });

            btn5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AdminDashboard.this, AdminEvents.class));
                }
            });

            btn6.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AdminDashboard.this, AdminPhotos.class));
                }
            });

            btn7.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseAuth.getInstance().signOut();
                    finish();
                    startActivity(new Intent(AdminDashboard.this, Login.class));
                }
            });
        }
    }

    public boolean isConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting())) return true;
            else return false;
        } else
            return false;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Warning");
        alertDialogBuilder
                .setMessage("Do you really want to exit?")
                .setCancelable(false)
                .setPositiveButton("EXIT",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // what to do if YES is tapped
                                finishAffinity();
                                System.exit(0);
                            }
                        })
                .setNegativeButton("CANCLE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // code to do on NO tapped
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
