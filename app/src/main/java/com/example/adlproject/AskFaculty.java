package com.example.adlproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class AskFaculty extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Toolbar mToolbar;

    private DatabaseReference usersRef, facultyRef, requestRef, facultyMeetingRef, backupRef;
    private FirebaseAuth mAuth;

    private String current_user_id, currentDepartment;

    private ProgressDialog loadingBar;

    private FirebaseRecyclerAdapter<Faculty, FacultyViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_faculty);

        mToolbar = findViewById(R.id.ask_faculty_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Ask Faculty");

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        currentDepartment = getIntent().getExtras().get("Depart").toString();
        Log.i("Depart", currentDepartment);
        requestRef = FirebaseDatabase.getInstance().getReference().child("Requests");
        facultyMeetingRef = FirebaseDatabase.getInstance().getReference().child("FacultyMeetingRequests");
        backupRef = FirebaseDatabase.getInstance().getReference().child("Backup").child("Meetings");

        loadingBar = new ProgressDialog(this);

        recyclerView = findViewById(R.id.ask_faculty_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayFaculty();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    private void displayFaculty() {
        facultyRef = FirebaseDatabase.getInstance().getReference().child("Faculty").child(currentDepartment);

        FirebaseRecyclerOptions<Faculty> options =
                new FirebaseRecyclerOptions.Builder<Faculty>()
                .setQuery(facultyRef, Faculty.class)
                .build();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Faculty, FacultyViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FacultyViewHolder holder, int position, @NonNull final Faculty model) {
                        final String postKey = getRef(position).getKey();

                        holder.textView.setText(model.getName());
                        Picasso.get().load(model.getProfileImage()).into(holder.circleImageView);

                        holder.button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestDialog(postKey, model.getName());
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FacultyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_students_faculty_activity, parent, false);
                        FacultyViewHolder viewHolder = new FacultyViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void requestDialog(final String postKey, final String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Problem Statement");

        final TextInputEditText textInputEditText = new TextInputEditText(this);
        textInputEditText.setHint("Problem");
        textInputEditText.setInputType(InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);

        builder.setView(textInputEditText);

        builder.setCancelable(false)
                .setPositiveButton("REQUEST",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        // what to do if YES is tapped
                        if (!TextUtils.isEmpty(textInputEditText.getText().toString())) {
                            loadingBar.setTitle("Request");
                            loadingBar.setMessage("Request is adding");
                            loadingBar.show();
                            loadingBar.setCanceledOnTouchOutside(true);
                            Calendar calDate = Calendar.getInstance();
                            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                            final String saveDate = currentDate.format(calDate.getTime());

                            Calendar calTime = Calendar.getInstance();
                            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
                            final String saveTime = currentTime.format(calTime.getTime());

                            final String requestKey = current_user_id + saveDate + saveTime;

                            usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        final String studentName = dataSnapshot.child("name").getValue().toString();
                                        final String en_no = dataSnapshot.child("enrolment_no").getValue().toString();

                                        HashMap postMap = new HashMap();
                                        postMap.put("studentUId", current_user_id);
                                        postMap.put("studentName", studentName);
                                        postMap.put("Problem", textInputEditText.getText().toString());
                                        postMap.put("status", "Pending");
                                        postMap.put("facultyUId", postKey);
                                        postMap.put("facultyName", name);
                                        postMap.put("date", saveDate);
                                        postMap.put("time", saveTime);
                                        postMap.put("en_no", en_no);

                                        requestRef.child(current_user_id).child(requestKey).updateChildren(postMap)
                                                .addOnCompleteListener(new OnCompleteListener() {
                                                    @Override
                                                    public void onComplete(@NonNull Task task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(AskFaculty.this, "Request Added", Toast.LENGTH_LONG).show();
                                                        } else {
                                                            String msg = task.getException().getMessage();
                                                            Toast.makeText(AskFaculty.this, "Error Occured: " + msg, Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });

                                        backupRef.child(en_no).child(requestKey).updateChildren(postMap)
                                                .addOnCompleteListener(new OnCompleteListener() {
                                                    @Override
                                                    public void onComplete(@NonNull Task task) {
                                                        //
                                                    }
                                                });

                                        facultyMeetingRef.child(postKey).child(requestKey).updateChildren(postMap)
                                                .addOnCompleteListener(new OnCompleteListener() {
                                                    @Override
                                                    public void onComplete(@NonNull Task task) {
                                                        if (task.isSuccessful()) {
                                                            Intent intent = new Intent(AskFaculty.this, FacultyHelp.class);
                                                            intent.putExtra("Depart", currentDepartment);
                                                            intent.putExtra("en_no", en_no);
                                                            startActivity(intent);
                                                            loadingBar.dismiss();
                                                        } else {
                                                            String msg = task.getException().getMessage();
                                                            Toast.makeText(AskFaculty.this, "Error Occured: " + msg, Toast.LENGTH_LONG).show();
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
                        else {
                            Toast.makeText(AskFaculty.this, "You have to write problem statement!!!", Toast.LENGTH_LONG).show();
                        }
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
        Dialog dialog = builder.create();
        dialog.show();
    }

    public static class FacultyViewHolder extends RecyclerView.ViewHolder{

        private TextView textView;
        private CircleImageView circleImageView;
        private Button button;

        public FacultyViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.faculty_name);
            button = itemView.findViewById(R.id.faculty_request_button);
            circleImageView = itemView.findViewById(R.id.faculty_profile_image);
        }
    }
}
