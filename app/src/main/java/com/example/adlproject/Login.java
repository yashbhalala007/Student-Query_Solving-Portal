package com.example.adlproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private EditText email, pass;
    private TextView forgetPass;
    private Button loginButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.login_email);
        pass = findViewById(R.id.login_password);
        forgetPass = findViewById(R.id.login_forget_pass_text);
        loginButton = findViewById(R.id.login_button);

        mAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(this);

        forgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Warning");
        alertDialogBuilder
                .setMessage("Do you really want to exit?")
                .setCancelable(false)
                .setPositiveButton("YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // what to do if YES is tapped
                                finishAffinity();
                                System.exit(0);
                            }
                        })
                .setNegativeButton("NO",
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

    private boolean validateEmail() {
        String emailInput = email.getText().toString().trim();

        if(emailInput.isEmpty()){
            email.setError("Field can't be empty");
            return false;
        } else if(!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()){
            email.setError("Please enter a valid email address");
            return false;
        } else {
            email.setError(null);
            return true;
        }
    }

    private boolean validatePassword(){
        String passInput = pass.getText().toString().trim();

        if(passInput.isEmpty()){
            pass.setError("Field can't be empty");
            return false;
        } else {
            pass.setError(null);
            return true;
        }
    }

    @Override
    public void onClick(View v) {
        loginUser();
    }

    private void loginUser() {
        if (!validateEmail()|| !validatePassword()) {
            Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show();
            return;
        }
        String emailInput = email.getText().toString().trim();
        String passInput = pass.getText().toString().trim();
        mAuth.signInWithEmailAndPassword(emailInput, passInput)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
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
                            //Intent intent = new Intent(getApplicationContext(), Dashboard.class);
                            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            //startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(Login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
