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

public class FacultyConfirmRequest extends Fragment {

    private RecyclerView recyclerView;
    private DatabaseReference usersRef, requestRef, backupRef;
    private FirebaseAuth mAuth;

    private String current_user_id, en_no, role;

    private FirebaseRecyclerAdapter<PandRrequests, confirmRequestViewHolder> firebaseRecyclerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.faculty_confirm_request, container, false);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        role = getArguments().getString("role");
        en_no = getArguments().getString("en_no");
        backupRef = FirebaseDatabase.getInstance().getReference().child("Backup").child("Meetings");
        requestRef = FirebaseDatabase.getInstance().getReference().child("Requests").child(current_user_id);
        recyclerView = view.findViewById(R.id.confirm_faculty_list);
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
            AdmindisplayConfirmRequest();
        }
        else {
            displayConfirmRequest();
        }

    }

    private void AdmindisplayConfirmRequest() {
        backupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(en_no).exists()){
                    Query query = backupRef.child(en_no).orderByChild("status")
                            .startAt("Confirmed").endAt("Confirmed" + "\uf8ff");

                    FirebaseRecyclerOptions<PandRrequests> options =
                            new FirebaseRecyclerOptions.Builder<PandRrequests>()
                                    .setQuery(query, PandRrequests.class)
                                    .build();

                    firebaseRecyclerAdapter =
                            new FirebaseRecyclerAdapter<PandRrequests, confirmRequestViewHolder>(options) {
                                @Override
                                protected void onBindViewHolder(@NonNull final confirmRequestViewHolder holder, int position, @NonNull final PandRrequests model) {
                                    final String postKey = getRef(position).getKey();

                                    holder.crname.setText(model.getFacultyName());
                                    holder.crdate.setText(model.getDate());
                                    holder.crproblem.setText(model.getProblem());
                                    backupRef.child(en_no).child(postKey).child("Confirmation").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            holder.crmdate.setText(dataSnapshot.child("Date").getValue().toString());
                                            holder.crmtime.setText(dataSnapshot.child("Time").getValue().toString());
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }

                                @NonNull
                                @Override
                                public confirmRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.status_confirmed_layout, parent, false);
                                    confirmRequestViewHolder viewHolder = new confirmRequestViewHolder(view);
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

    private void displayConfirmRequest() {

        requestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Query query = requestRef.orderByChild("status")
                            .startAt("Confirmed").endAt("Confirmed" + "\uf8ff");

                    FirebaseRecyclerOptions<PandRrequests> options =
                            new FirebaseRecyclerOptions.Builder<PandRrequests>()
                                    .setQuery(query, PandRrequests.class)
                                    .build();

                    firebaseRecyclerAdapter =
                            new FirebaseRecyclerAdapter<PandRrequests, confirmRequestViewHolder>(options) {
                                @Override
                                protected void onBindViewHolder(@NonNull final confirmRequestViewHolder holder, int position, @NonNull final PandRrequests model) {
                                    final String postKey = getRef(position).getKey();

                                    holder.crname.setText(model.getFacultyName());
                                    holder.crdate.setText(model.getDate());
                                    holder.crproblem.setText(model.getProblem());
                                    requestRef.child(postKey).child("Confirmation").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            holder.crmdate.setText(dataSnapshot.child("Date").getValue().toString());
                                            holder.crmtime.setText(dataSnapshot.child("Time").getValue().toString());
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }

                                @NonNull
                                @Override
                                public confirmRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.status_confirmed_layout, parent, false);
                                    confirmRequestViewHolder viewHolder = new confirmRequestViewHolder(view);
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

    public static class confirmRequestViewHolder extends RecyclerView.ViewHolder{

        private TextView crname, crdate, crproblem, crmdate, crmtime;

        public confirmRequestViewHolder(@NonNull View itemView) {
            super(itemView);

            crname = itemView.findViewById(R.id.status_confirmed_faculty_name);
            crdate = itemView.findViewById(R.id.status_confirmed_request_date);
            crproblem = itemView.findViewById(R.id.status_confirmed_problem);
            crmdate = itemView.findViewById(R.id.status_confirmed_meeting_date);
            crmtime = itemView.findViewById(R.id.status_confirmed_meeting_time);
        }
    }
}
