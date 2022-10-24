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

public class FacultyMeetingRejected extends Fragment {

    private RecyclerView recyclerView;
    private DatabaseReference usersRef, requestRef, facultyMeetingRef;
    private FirebaseAuth mAuth;

    private String current_user_id;

    private FirebaseRecyclerAdapter<PandRrequests, RMeetingViewHolder> firebaseRecyclerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.faculty_meeting_rejected, container, false);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        requestRef = FirebaseDatabase.getInstance().getReference().child("Requests");
        facultyMeetingRef = FirebaseDatabase.getInstance().getReference().child("FacultyMeetingRequests").child(current_user_id);

        recyclerView = view.findViewById(R.id.rejected_meeting_list);
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
        displayRejectedList();
    }

    private void displayRejectedList() {
        facultyMeetingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Query query = facultyMeetingRef.orderByChild("status")
                            .startAt("Rejected").endAt("Rejected" + "\uf8ff");

                    FirebaseRecyclerOptions<PandRrequests> options =
                            new FirebaseRecyclerOptions.Builder<PandRrequests>()
                                    .setQuery(query, PandRrequests.class)
                                    .build();

                    firebaseRecyclerAdapter =
                            new FirebaseRecyclerAdapter<PandRrequests, RMeetingViewHolder>(options) {
                                @Override
                                protected void onBindViewHolder(@NonNull RMeetingViewHolder holder, int position, @NonNull PandRrequests model) {

                                    holder.sName.setText(model.getStudentName());
                                    holder.pdate.setText(model.getDate());
                                    holder.pProblem.setText(model.getProblem());
                                }

                                @NonNull
                                @Override
                                public RMeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.faculty_meeting_rejected_layout, parent, false);
                                    RMeetingViewHolder viewHolder = new RMeetingViewHolder(view);
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

    public static class RMeetingViewHolder extends RecyclerView.ViewHolder{

        private TextView sName, pdate, pProblem;

        public RMeetingViewHolder(@NonNull View itemView) {
            super(itemView);

            sName = itemView.findViewById(R.id.meeting_rejected_student_name);
            pdate = itemView.findViewById(R.id.meeting_rejected_request_date);
            pProblem = itemView.findViewById(R.id.meeting_rejected_problem);
        }
    }
}
