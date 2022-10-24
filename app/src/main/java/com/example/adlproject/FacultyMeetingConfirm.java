package com.example.adlproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class FacultyMeetingConfirm extends Fragment {

    private RecyclerView recyclerView;
    private DatabaseReference usersRef, requestRef, facultyMeetingRef;
    private FirebaseAuth mAuth;

    private String current_user_id;

    private FirebaseRecyclerAdapter<PandRrequests, CMeetingViewHolder> firebaseRecyclerAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.faculty_meeting_confirm, container, false);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        requestRef = FirebaseDatabase.getInstance().getReference().child("Requests");
        facultyMeetingRef = FirebaseDatabase.getInstance().getReference().child("FacultyMeetingRequests").child(current_user_id);

        recyclerView = view.findViewById(R.id.confirm_meeting_list);
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
        displayConfirmedList();
    }

    private void displayConfirmedList() {
        facultyMeetingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Query query = facultyMeetingRef.orderByChild("status")
                            .startAt("Confirmed").endAt("Confirmed" + "\uf8ff");

                    FirebaseRecyclerOptions<PandRrequests> options =
                            new FirebaseRecyclerOptions.Builder<PandRrequests>()
                                    .setQuery(query, PandRrequests.class)
                                    .build();

                    firebaseRecyclerAdapter =
                            new FirebaseRecyclerAdapter<PandRrequests, CMeetingViewHolder>(options) {
                                @Override
                                protected void onBindViewHolder(@NonNull final CMeetingViewHolder holder, int position, @NonNull PandRrequests model) {
                                    final String postKey = getRef(position).getKey();

                                    holder.sName.setText(model.getStudentName());
                                    holder.pdate.setText(model.getDate());
                                    holder.pProblem.setText(model.getProblem());

                                    facultyMeetingRef.child(postKey).child("Confirmation").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            holder.pDate.setText(dataSnapshot.child("Date").getValue().toString());
                                            holder.pTime.setText(dataSnapshot.child("Time").getValue().toString());
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }

                                @NonNull
                                @Override
                                public CMeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.faculty_meeting_confirmed_layout, parent, false);
                                    CMeetingViewHolder viewHolder = new CMeetingViewHolder(view);
                                    return viewHolder;
                                }
                            };

                    recyclerView.setAdapter(firebaseRecyclerAdapter);
                    firebaseRecyclerAdapter.startListening();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }

    public static class CMeetingViewHolder extends RecyclerView.ViewHolder{

        private TextView sName, pdate, pProblem, pDate, pTime;

        public CMeetingViewHolder(@NonNull View itemView) {
            super(itemView);

            sName = itemView.findViewById(R.id.meeting_confirmed_student_name);
            pdate = itemView.findViewById(R.id.meeting_confirmed_request_date);
            pProblem = itemView.findViewById(R.id.meeting_confirmed_problem);
            pDate = itemView.findViewById(R.id.meeting_confirmed_meeting_date);
            pTime = itemView.findViewById(R.id.meeting_confirmed_meeting_time);
        }
    }

}
