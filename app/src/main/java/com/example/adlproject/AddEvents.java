package com.example.adlproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddEvents extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton ib1, ib2;
    private EditText ed2;
    private Button btn;
    private TextView txt;
    private Spinner stateSpinner, citySpinner;

    final static int Gallery_Pick = 1;
    private Uri ImageUri;
    private String description;
    private ProgressDialog loadingBar;

    private StorageReference eventReference;
    private DatabaseReference usersRef, eventRef, backupRef;
    private FirebaseAuth mAuth;

    private String saveDate, saveTime, postName, current_user_id, date;
    private String  downloadUrl, selectedCity = "Nothing Selected", selectedState = "Nothing Selected";
    private long countEvents = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_events);

        ib1 = findViewById(R.id.event_date_button);
        txt = findViewById(R.id.event_display_date);
        ib2 = findViewById(R.id.event_pic);
        ed2 = findViewById(R.id.event_des);
        btn = findViewById(R.id.event_send);
        stateSpinner = findViewById(R.id.event_select_state);
        citySpinner = findViewById(R.id.event_select_city);

        mToolbar = findViewById(R.id.add_event_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Add Event");

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
        eventReference = FirebaseStorage.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        eventRef = FirebaseDatabase.getInstance().getReference().child("Events");
        backupRef = FirebaseDatabase.getInstance().getReference().child("Backup").child("Events");

        loadingBar = new ProgressDialog(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.states, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(adapter);
        stateSpinner.setSelection(0);

        final ArrayAdapter<CharSequence>[] cityAdapter = new ArrayAdapter[]{ArrayAdapter.createFromResource(this, R.array.DefaultCity, android.R.layout.simple_spinner_item)};
        cityAdapter[0].setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(cityAdapter[0]);
        citySpinner.setSelection(0);

        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position){
                    case 0:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.DefaultCity, android.R.layout.simple_spinner_item);
                        selectedState = "Nothing Selected";
                        selectedCity = "Nothing Selected";
                        break;
                    case 1:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s1, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 2:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s2, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 3:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s3, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 4:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s4, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 5:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s5, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 6:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s6, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 7:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s7, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 8:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s8, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 9:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s9, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 10:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s10, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 11:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s11, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 12:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s12, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 13:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s13, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 14:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s14, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 15:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s15, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 16:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s16, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 17:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s17, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 18:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s18, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 19:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s19, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 20:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s20, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 21:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s21, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 22:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s22, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 23:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s23, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 24:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s24, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 25:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s25, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 26:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s26, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 27:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s27, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 28:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s28, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 29:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s29, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 30:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s30, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 31:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s31, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 32:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s32, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;
                    case 33:
                        cityAdapter[0] = ArrayAdapter.createFromResource(AddEvents.this, R.array.s33, android.R.layout.simple_spinner_item);
                        selectedState = stateSpinner.getSelectedItem().toString();
                        break;

                }
                citySpinner.setAdapter(cityAdapter[0]);
                citySpinner.setSelection(0);

                Log.i("ItemPos", selectedState);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    selectedCity = "Nothing Selected";
                }
                else {
                    selectedCity = citySpinner.getSelectedItem().toString();
                }
                Log.i("ItemPos", selectedCity);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ib1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(v);
            }
        });

        ib2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });
    }

    public static class MyDatePickerFragment extends DialogFragment {

        public MyDatePickerFragment(TextView txt2) {
            this.txt2 = txt2;
        }

        TextView txt2;

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), dateSetListener, year, month, day);
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            return datePickerDialog;
        }

        private void updateDisplay(String date){
            txt2.setText(date);
        }

        private DatePickerDialog.OnDateSetListener dateSetListener =
                new DatePickerDialog.OnDateSetListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    public void onDateSet(DatePicker view, int year, int month, int day) {

                        Calendar cal=Calendar.getInstance();
                        android.icu.text.SimpleDateFormat month_date = new android.icu.text.SimpleDateFormat("MMMM");
                        cal.set(Calendar.MONTH,month);

                        String selectedDate = day + "-" + month_date.format(cal.getTime()) + "-" + year;
                        updateDisplay(selectedDate);
                    }
                };
    }


    public void showDatePicker(View v) {
        DialogFragment newFragment = new MyDatePickerFragment(txt);
        newFragment.show(getSupportFragmentManager(), "date picker");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if(id == android.R.id.home){
            Intent intent = new Intent(AddEvents.this, Events.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void ValidatePostInfo() {
        description = ed2.getText().toString();
        if (ImageUri == null){
            Toast.makeText(AddEvents.this,"Please Select Image First!!", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(description)){
            Toast.makeText(AddEvents.this,"Please Write Description First!!", Toast.LENGTH_LONG).show();
        }
        else if (selectedState.equals("Nothing Selected")){
            Toast.makeText(AddEvents.this,"Please Write State First!!", Toast.LENGTH_LONG).show();
        }
        else if (selectedCity.equals("Nothing Selected")){
            Toast.makeText(AddEvents.this,"Please Write City First!!", Toast.LENGTH_LONG).show();
        }
        else {
            loadingBar.setTitle("Post");
            loadingBar.setMessage("Post is adding");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            StoringImage();
        }
    }

    private void StoringImage() {
        Calendar calDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveDate = currentDate.format(calDate.getTime());

        Calendar calTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveTime = currentTime.format(calTime.getTime());

        postName = current_user_id + saveDate + saveTime;
        final StorageReference filePath = eventReference.child("Event Images").child(ImageUri.getLastPathSegment() + postName + ".jpg");
        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){

                    filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            downloadUrl = task.getResult().toString();
                            SavingPostInfo();
                        }
                    });
                }
                else {
                    String msg = task.getException().getMessage();
                    Toast.makeText(AddEvents.this,"Error Occured: " + msg, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void SavingPostInfo() {

        eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    countEvents = dataSnapshot.getChildrenCount();
                }
                else {
                    countEvents = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

            usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()){
                        String userFullName = dataSnapshot.child("name").getValue().toString();
                        String en_no = dataSnapshot.child("enrolment_no").getValue().toString();

                        HashMap postMap = new HashMap();
                        postMap.put("uid", current_user_id);
                        postMap.put("date", saveDate);
                        postMap.put("time", saveTime);
                        postMap.put("eventDate", txt.getText().toString());
                        postMap.put("city", selectedCity + ", " + selectedState);
                        postMap.put("description", description);
                        postMap.put("postImage", downloadUrl);
                        postMap.put("name", userFullName);
                        postMap.put("counter", countEvents);
                        postMap.put("profileImage", dataSnapshot.child("profileImage").getValue().toString());

                        eventRef.child(postName).updateChildren(postMap)
                                .addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {

                                    }
                                });

                        postMap.put("enrolmentNo",en_no);
                        postMap.put("status", "Active");
                        backupRef.child(en_no).child(postName).updateChildren(postMap)
                                .addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if(task.isSuccessful()){

                                            Intent intent = new Intent(AddEvents.this, Events.class);
                                            startActivity(intent);
                                            Toast.makeText(AddEvents.this,"Post added Successfully!!!", Toast.LENGTH_LONG).show();
                                            loadingBar.dismiss();
                                        }
                                        else {
                                            String msg = task.getException().getMessage();
                                            Toast.makeText(AddEvents.this,"Error Occured: " + msg, Toast.LENGTH_LONG).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                                });
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            ImageUri = data.getData();
            ib2.setImageURI(ImageUri);

        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AddEvents.this, Events.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
