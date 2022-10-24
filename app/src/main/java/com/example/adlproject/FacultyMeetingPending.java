package com.example.adlproject;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class FacultyMeetingPending extends Fragment {

    private RecyclerView recyclerView;
    private DatabaseReference usersRef, requestRef, facultyMeetingRef, backupRef;
    private FirebaseAuth mAuth;

    private String current_user_id;

    private FirebaseRecyclerAdapter<PandRrequests, PMeetingViewHolder> firebaseRecyclerAdapter;


    private FragmentActivity myContex;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.faculty_meeting_pending, container, false);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        requestRef = FirebaseDatabase.getInstance().getReference().child("Requests");
        facultyMeetingRef = FirebaseDatabase.getInstance().getReference().child("FacultyMeetingRequests").child(current_user_id);
        backupRef = FirebaseDatabase.getInstance().getReference().child("Backup").child("Meetings");

        recyclerView = view.findViewById(R.id.pending_meeting_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        displayPendingMeeting();
    }

    private void displayPendingMeeting() {
        facultyMeetingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Query query = facultyMeetingRef.orderByChild("status")
                            .startAt("Pending").endAt("Pending" + "\uf8ff");

                    FirebaseRecyclerOptions<PandRrequests> options =
                            new FirebaseRecyclerOptions.Builder<PandRrequests>()
                                    .setQuery(query, PandRrequests.class)
                                    .build();

                    firebaseRecyclerAdapter =
                            new FirebaseRecyclerAdapter<PandRrequests, PMeetingViewHolder>(options) {
                                @Override
                                protected void onBindViewHolder(@NonNull PMeetingViewHolder holder, int position, @NonNull PandRrequests model) {
                                    final String postKey = getRef(position).getKey();

                                    holder.sName.setText(model.getStudentName());
                                    holder.pdate.setText(model.getDate());
                                    holder.pProblem.setText(model.getProblem());
                                    holder.btn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            confirmMeetingDialog(postKey);
                                        }
                                    });

                                    holder.btn2.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            rejectMeetingDialog(postKey);
                                        }
                                    });
                                }

                                @NonNull
                                @Override
                                public PMeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.faculty_meeting_pending_layout, parent, false);
                                    PMeetingViewHolder viewHolder = new PMeetingViewHolder(view);
                                    return viewHolder;
                                }
                            };

                    recyclerView.setAdapter(firebaseRecyclerAdapter);
                    firebaseRecyclerAdapter.startListening();
                }
                else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

    }

    private void rejectMeetingDialog(final String postKey) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setTitle("Warning");
        alertDialogBuilder
                .setMessage("Do you really want to reject meeting?")
                .setCancelable(false)
                .setPositiveButton("REJECT",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // what to do if YES is tapped
                                facultyMeetingRef.child(postKey).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()){
                                            final String studentUid = dataSnapshot.child("studentUId").getValue().toString();
                                            String en_no = dataSnapshot.child("en_no").getValue().toString();
                                            facultyMeetingRef.child(postKey).child("status").setValue("Rejected");
                                            requestRef.child(studentUid).child(postKey).child("status").setValue("Rejected");
                                            backupRef.child(en_no).child(postKey).child("status").setValue("Rejected");
                                            Toast.makeText(getContext(), "Meeting Rejected SuccessFully", Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
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

    private void confirmMeetingDialog(final String postKey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Meeting Confirmation");

        LinearLayout linearLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linearLayout1 = new LinearLayout(getContext());
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout1.setOrientation(LinearLayout.HORIZONTAL);

        final ImageButton imageButton = new ImageButton(getContext());
        imageButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.calendar_dialog));
        imageButton.setLayoutParams(params1);

        final TextView textView = new TextView(getContext());
        textView.setTextSize(14);
        textView.setLayoutParams(params1);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(v, textView);
            }
        });

        linearLayout1.addView(imageButton);
        linearLayout1.addView(textView);

        linearLayout.addView(linearLayout1);

        LinearLayout linearLayout2 = new LinearLayout(getContext());
        linearLayout2.setOrientation(LinearLayout.HORIZONTAL);

        final ImageButton imageButton2 = new ImageButton(getContext());
        imageButton2.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.time_dialog));
        imageButton2.setLayoutParams(params1);

        final TextView textView1 = new TextView(getContext());
        textView1.setTextSize(14);
        textView1.setLayoutParams(params1);

        imageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(v, textView1);
            }
        });

        linearLayout2.addView(imageButton2);
        linearLayout2.addView(textView1);

        linearLayout.addView(linearLayout2);

        builder.setView(linearLayout);

        builder.setCancelable(false)
                .setPositiveButton("UPDATE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // what to do if YES is tapped
                                if (!TextUtils.isEmpty(textView.getText().toString()) && !TextUtils.isEmpty(textView1.getText().toString())){
                                    final String selectedDate = textView.getText().toString();
                                    final String selectedTime = textView1.getText().toString();

                                    facultyMeetingRef.child(postKey).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                                            if (dataSnapshot.exists()) {

                                                final String studentUid = dataSnapshot.child("studentUId").getValue().toString();
                                                final String en_no = dataSnapshot.child("en_no").getValue().toString();

                                                HashMap postMap = new HashMap();
                                                postMap.put("Date", selectedDate);
                                                postMap.put("Time", selectedTime);

                                                facultyMeetingRef.child(postKey).child("Confirmation").updateChildren(postMap)
                                                        .addOnCompleteListener(new OnCompleteListener() {
                                                            @Override
                                                            public void onComplete(@NonNull Task task) {
                                                                if (task.isSuccessful()){
                                                                    Toast.makeText(getContext(),"Added SuccessFully", Toast.LENGTH_LONG).show();
                                                                    facultyMeetingRef.child(postKey).child("status").setValue("Confirmed");
                                                                }
                                                                else {
                                                                    Toast.makeText(getContext(),"Error 1", Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });

                                                backupRef.child(en_no).child(postKey).child("Confirmation").updateChildren(postMap)
                                                        .addOnCompleteListener(new OnCompleteListener() {
                                                            @Override
                                                            public void onComplete(@NonNull Task task) {
                                                                if (task.isSuccessful()){
                                                                    Toast.makeText(getContext(),"Added SuccessFully", Toast.LENGTH_LONG).show();
                                                                    backupRef.child(en_no).child(postKey).child("status").setValue("Confirmed");
                                                                }
                                                                else {
                                                                    Toast.makeText(getContext(),"Error 3", Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });

                                                requestRef.child(studentUid).child(postKey).child("Confirmation").updateChildren(postMap)
                                                        .addOnCompleteListener(new OnCompleteListener() {
                                                            @Override
                                                            public void onComplete(@NonNull Task task) {
                                                                if (task.isSuccessful()){
                                                                    Toast.makeText(getContext(),"Added SuccessFully", Toast.LENGTH_LONG).show();
                                                                    requestRef.child(studentUid).child(postKey).child("status").setValue("Confirmed");
                                                                }
                                                                else {
                                                                    Toast.makeText(getContext(),"Error 2", Toast.LENGTH_LONG).show();
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
                                    Toast.makeText(getContext(), "Please, Select Date and Time For Meeting", Toast.LENGTH_LONG).show();
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

    private void showTimePicker(View v, TextView textView1) {
        DialogFragment newFragment1 = new DateandTimePicker(textView1);
        newFragment1.show(myContex.getSupportFragmentManager(), "time picker");
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        myContex = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    public void showDatePicker(View v, TextView txt) {
        DialogFragment newFragment = new AddEvents.MyDatePickerFragment(txt);
        newFragment.show(myContex.getSupportFragmentManager(), "date picker");
    }



    public static class PMeetingViewHolder extends RecyclerView.ViewHolder{

        private TextView sName, pdate, pProblem;
        private Button btn, btn2;

        public PMeetingViewHolder(@NonNull View itemView) {
            super(itemView);

            sName = itemView.findViewById(R.id.meeting_pending_student_name);
            pdate = itemView.findViewById(R.id.meeting_pending_request_date);
            pProblem = itemView.findViewById(R.id.meeting_pending_problem);
            btn = itemView.findViewById(R.id.meeting_pending_button);
            btn2 = itemView.findViewById(R.id.meeting_pending_Rejection_button);
        }
    }
}
