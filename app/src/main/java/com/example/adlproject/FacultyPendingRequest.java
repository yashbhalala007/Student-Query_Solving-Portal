package com.example.adlproject;

import android.content.Intent;
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
import androidx.viewpager.widget.ViewPager;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class FacultyPendingRequest extends Fragment {

    private RecyclerView recyclerView;
    private DatabaseReference usersRef, requestRef, backupRef;
    private FirebaseAuth mAuth;

    private String current_user_id, en_no, role;

    private FirebaseRecyclerAdapter<PandRrequests, PrequestsViewHolder> firebaseRecyclerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.faculty_pending_request, container, false);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        role = getArguments().get("role").toString();
        en_no = getArguments().get("en_no").toString();
        backupRef = FirebaseDatabase.getInstance().getReference().child("Backup").child("Meetings");
        requestRef = FirebaseDatabase.getInstance().getReference().child("Requests").child(current_user_id);
        recyclerView = view.findViewById(R.id.pending_faculty_list);
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
                            .startAt("Pending").endAt("Pending" + "\uf8ff");

                    FirebaseRecyclerOptions<PandRrequests> options =
                            new FirebaseRecyclerOptions.Builder<PandRrequests>()
                                    .setQuery(query, PandRrequests.class)
                                    .build();

                    firebaseRecyclerAdapter =
                            new FirebaseRecyclerAdapter<PandRrequests, PrequestsViewHolder>(options) {
                                @Override
                                protected void onBindViewHolder(@NonNull PrequestsViewHolder holder, int position, @NonNull PandRrequests model) {
                                    holder.fName.setText(model.getFacultyName());
                                    holder.date.setText(model.getDate());
                                    holder.problem.setText(model.getProblem());
                                }

                                @NonNull
                                @Override
                                public PrequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.status_pending_layout, parent, false);
                                    PrequestsViewHolder viewHolder = new PrequestsViewHolder(view);
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
                            .startAt("Pending").endAt("Pending" + "\uf8ff");

                    FirebaseRecyclerOptions<PandRrequests> options =
                            new FirebaseRecyclerOptions.Builder<PandRrequests>()
                            .setQuery(query, PandRrequests.class)
                            .build();

                    firebaseRecyclerAdapter =
                            new FirebaseRecyclerAdapter<PandRrequests, PrequestsViewHolder>(options) {
                                @Override
                                protected void onBindViewHolder(@NonNull PrequestsViewHolder holder, int position, @NonNull PandRrequests model) {
                                    holder.fName.setText(model.getFacultyName());
                                    holder.date.setText(model.getDate());
                                    holder.problem.setText(model.getProblem());
                                }

                                @NonNull
                                @Override
                                public PrequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.status_pending_layout, parent, false);
                                    PrequestsViewHolder viewHolder = new PrequestsViewHolder(view);
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

    public static class PrequestsViewHolder extends RecyclerView.ViewHolder {

        private TextView fName, date, problem;

        public PrequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            fName = itemView.findViewById(R.id.status_pending_faculty_name);
            date = itemView.findViewById(R.id.status_pending_request_date);
            problem = itemView.findViewById(R.id.status_pending_problem);
        }
    }
}
