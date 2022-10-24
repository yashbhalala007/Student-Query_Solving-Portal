package com.example.adlproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminMeetings extends AppCompatActivity {
    private RecyclerView recyclerView;
    private Toolbar mToolbar;
    private EditText editText;

    private DatabaseReference userRef;
    private FirebaseAuth mAuth;

    private String current_user_id, dept;

    private FirebaseRecyclerAdapter<EventsList, AdminMeetingsViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_meetings);

        mToolbar = findViewById(R.id.admin_meetings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Meetings");
        editText = findViewById(R.id.admin_meetings_search);


        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        userRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    dept = dataSnapshot.child("department").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //set post view
        recyclerView = findViewById(R.id.admin_meetings_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(false);
        recyclerView.setLayoutManager(linearLayoutManager);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String search = editText.getText().toString().trim();
                if (TextUtils.isEmpty(search)){
                    Query query = userRef.orderByChild("enrolment_no");
                    displayAllUser(query);
                }
                else {
                    Query query = userRef.orderByChild("enrolment_no")
                            .startAt(search).endAt(search + "\uf8ff");
                    displayAllUser(query);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Query query = userRef.orderByChild("enrolment_no");
        displayAllUser(query);
    }

    private void displayAllUser(Query query) {

        FirebaseRecyclerOptions<EventsList> options =
                new FirebaseRecyclerOptions.Builder<EventsList>()
                        .setQuery(query, EventsList.class)
                        .build();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<EventsList, AdminMeetingsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final AdminMeetingsViewHolder holder, int position, @NonNull final EventsList model) {
                        final String postKey = getRef(position).getKey();
                        final String uid = model.getUid();

                        holder.username.setText(model.getName());
                        holder.en_no.setText(model.getEnrolment_no());
                        Picasso.get().load(model.getProfileImage()).into(holder.profileImage);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent = new Intent(AdminMeetings.this, FacultyHelp.class);
                                clickPostIntent.putExtra("Depart", dept);
                                clickPostIntent.putExtra("en_no", model.getEnrolment_no());
                                startActivity(clickPostIntent);
                            }
                        });

                    }

                    @NonNull
                    @Override
                    public AdminMeetingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_meetings_user_list, parent, false);
                        AdminMeetingsViewHolder viewHolder = new AdminMeetingsViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class AdminMeetingsViewHolder extends RecyclerView.ViewHolder{

        private TextView username, en_no;
        private CircleImageView profileImage;

        public AdminMeetingsViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.admin_meeting_user_name);
            en_no = itemView.findViewById(R.id.admin_meeting_user_en_no);
            profileImage = itemView.findViewById(R.id.admin_meeting_profile_image);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AdminMeetings.this, AdminDashboard.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
