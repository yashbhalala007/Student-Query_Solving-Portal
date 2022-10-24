package com.example.adlproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputLayout name, email, en, user, pass, cpass;
    private Spinner spinner;
    private Button btn_r;
    private ProgressBar progressBar;
    private RadioGroup rg1, rg2;
    private RadioButton rb1, rb2;
    int passsame = 0;
    private String role = "", gen = "", depart = "", userName = "", enrolment_no = "", password = "", email_id = "", fullName = "";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = findViewById(R.id.il_name);
        email = findViewById(R.id.il_email);
        en = findViewById(R.id.il_en);
        user = findViewById(R.id.il_user);
        pass = findViewById(R.id.il_pass);
        cpass = findViewById(R.id.il_cpass);
        TextInputEditText repass = findViewById(R.id.ed_cpass);
        spinner = findViewById(R.id.dept);
        rg1 = findViewById(R.id.rg1);
        rg2 = findViewById(R.id.rg2);
        btn_r = findViewById(R.id.btn_reg);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

        btn_r.setOnClickListener(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.department, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                depart = spinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        repass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String strPass1 = pass.getEditText().getText().toString().trim();
                String strPass2 = cpass.getEditText().getText().toString().trim();

                if (!strPass1.equals(strPass2)) {
                    cpass.setError("Password not matched");
                    passsame = 0;
                } else {
                    cpass.setError(null);
                    passsame = 1;
                }
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null){
            //handle already login user
        }
    }

    private boolean validateEmail() {
        String emailInput = email.getEditText().getText().toString().trim();

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
        String passInput = pass.getEditText().getText().toString().trim();

        if(passInput.isEmpty()){
            pass.setError("Field can't be empty");
            return false;
        } else if(passInput.length() < 6 && passInput.length() > 15){
            pass.setError("Please enter a valid password");
            return false;
        } else {
            pass.setError(null);
            return true;
        }
    }

    private boolean validateName(){
        String nameInput = name.getEditText().getText().toString().trim();

        if(nameInput.isEmpty()){
            name.setError("Field can't be empty");
            return false;
        } else if(nameInput.length() > 50){
            name.setError("Please enter a valid Name");
            return false;
        } else {
            name.setError(null);
            return true;
        }
    }

    private boolean validateUsername(){
        String userInput = user.getEditText().getText().toString().trim();

        if(userInput.isEmpty()){
            user.setError("Field can't be empty");
            return false;
        } else if(userInput.length() > 10){
            user.setError("Please enter a valid Username");
            return false;
        } else {
            user.setError(null);
            return true;
        }
    }

    private boolean validateEnrolment(){
        String enInput = en.getEditText().getText().toString().trim();

        if(enInput.isEmpty()){
            en.setError("Field can't be empty");
            return false;
        } else if(enInput.length() > 11){
            en.setError("Please enter a valid Enrolment");
            return false;
        } else {
            en.setError(null);
            return true;
        }
    }


    private void resetForm() {
        email.getEditText().setText(null);
        name.getEditText().setText(null);
        en .getEditText().setText(null);
        user.getEditText().setText(null);
        pass.getEditText().setText(null);
        cpass.getEditText().setText(null);
        rb1.setChecked(false);
        rb2.setChecked(false);
        spinner.setSelection(0);
    }

    int selectedId;
    private void registerUser() {
        selectedId = rg1.getCheckedRadioButtonId();
        rb1 = findViewById(selectedId);
        gen = rb1.getText().toString();
        selectedId = rg2.getCheckedRadioButtonId();
        rb2 = findViewById(selectedId);
        role = rb2.getText().toString();
        if (!validateEmail() || !validateEnrolment() || !validateName() || !validatePassword() || !validateUsername() || passsame == 0) {
            Toast.makeText(this, role, Toast.LENGTH_SHORT).show();
            return;
        }

        email_id = email.getEditText().getText().toString().trim();
        password = pass.getEditText().getText().toString().trim();
        fullName = name.getEditText().getText().toString().trim();
        enrolment_no = en.getEditText().getText().toString().trim();
        userName = user.getEditText().getText().toString().trim();


        String input = "Email: " + email_id;
        input += "\n";
        input += "Name: " + fullName;
        input += "\n";
        input += "Username: " + userName;
        input += "\n";
        input += "password: " + password;
        input += "\n";
        input += "En no: " + enrolment_no;
        input += "\n";
        input += "department: " + depart;
        input += "\n";
        input += "role: " + role;
        input += "\n";
        input += "gender: " + gen;
        input += "\n";
        Toast.makeText(this, input, Toast.LENGTH_SHORT).show();

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email_id, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            User user = new User(
                                    fullName,
                                    email_id,
                                    userName,
                                    role,
                                    gen,
                                    depart,
                                    enrolment_no
                            );

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        resetForm();
                                        Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_LONG).show();
                                    } else {
                                        //display a failure message
                                        resetForm();
                                        Toast.makeText(MainActivity.this, "Fail", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                        } else {
                            resetForm();
                            Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reg:
                registerUser();
                break;
        }
    }


}
