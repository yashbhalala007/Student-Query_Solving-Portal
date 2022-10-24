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

public class FacultyRejectedRequest extends Fragment {

    private RecyclerView recyclerView;
    private DatabaseReference usersRef, requestRef, backupRef;
    private FirebaseAuth mAuth;

    private String current_user_id, en_no, role;

    private FirebaseRecyclerAdapter<PandRrequests, RrequestsViewHolder> firebaseRecyclerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.faculty_rejected_request, container, false);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        role = getArguments().get("role").toString();
        en_no = getArguments().get("en_no").toString();
        backupRef = FirebaseDatabase.getInstance().getReference().child("Backup").child("Meetings");
        requestRef = FirebaseDatabase.getInstance().getReference().child("Requests").child(current_user_id);        recyclerView = view.findViewById(R.id.rejected_faculty_list);
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
        if (role.equals("Admin")){
            AdmindisplayPendingRequest();
        }
        else {
            displayPendingRequest();
        }
    }

    private void AdmindisplayPendingRequest() {
        backupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(en_no).exists()){
                    Query query = backupRef.child(en_no).orderByChild("status")
                            .startAt("Rejected").endAt("Rejected" + "\uf8ff");

                    FirebaseRecyclerOptions<PandRrequests> options =
                            new FirebaseRecyclerOptions.Builder<PandRrequests>()
                                    .setQuery(query, PandRrequests.class)
                                    .build();

                    firebaseRecyclerAdapter =
                            new FirebaseRecyclerAdapter<PandRrequests, RrequestsViewHolder>(options) {
                                @Override
                                protected void onBindViewHolder(@NonNull RrequestsViewHolder holder, int position, @NonNull PandRrequests model) {
                                    holder.fName.setText(model.getFacultyName());
                                    holder.date.setText(model.getDate());
                                    holder.problem.setText(model.getProblem());
                                }

                                @NonNull
                                @Override
                                public RrequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.status_rejected_layout, parent, false);
                                    RrequestsViewHolder viewHolder = new RrequestsViewHolder(view);
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

    private void displayPendingRequest() {
        requestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Query query = requestRef.orderByChild("status")
                            .startAt("Rejected").endAt("Rejected" + "\uf8ff");

                    FirebaseRecyclerOptions<PandRrequests> options =
                            new FirebaseRecyclerOptions.Builder<PandRrequests>()
                                    .setQuery(query, PandRrequests.class)
                                    .build();

                    firebaseRecyclerAdapter =
                            new FirebaseRecyclerAdapter<PandRrequests, RrequestsViewHolder>(options) {
                                @Override
                                protected void onBindViewHolder(@NonNull RrequestsViewHolder holder, int position, @NonNull PandRrequests model) {
                                    holder.fName.setText(model.getFacultyName());
                                    holder.date.setText(model.getDate());
                                    holder.problem.setText(model.getProblem());
                                }

                                @NonNull
                                @Override
                                public RrequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.status_rejected_layout, parent, false);
                                    RrequestsViewHolder viewHolder = new RrequestsViewHolder(view);
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

    public static class RrequestsViewHolder extends RecyclerView.ViewHolder {

        private TextView fName, date, problem;

        public RrequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            fName = itemView.findViewById(R.id.status_rejected_faculty_name);
            date = itemView.findViewById(R.id.status_rejected_request_date);
            problem = itemView.findViewById(R.id.status_rejected_problem);
        }
    }

}
