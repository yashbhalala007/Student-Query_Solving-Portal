package com.example.adlproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminEvents extends AppCompatActivity {
    private RecyclerView recyclerView;
    private Toolbar mToolbar;
    private EditText editText;

    private DatabaseReference backupRef;
    private FirebaseAuth mAuth;

    private String current_user_id;

    private FirebaseRecyclerAdapter<EventsList, AdminEventViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_events);

        mToolbar = findViewById(R.id.admin_events_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Events");
        editText = findViewById(R.id.admin_events_search);


        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
        backupRef = FirebaseDatabase.getInstance().getReference().child("Backup").child("Events");

        //set post view
        recyclerView = findViewById(R.id.admin_event_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
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
                    firebaseRecyclerAdapter.stopListening();
                }
                else {
                    displayAllUserEvents(search);
                }
            }
        });
    }

    private void displayAllUserEvents(String search) {

        Query eventQuery = backupRef.child(search).orderByChild("counter");

        FirebaseRecyclerOptions<EventsList> options =
                new FirebaseRecyclerOptions.Builder<EventsList>()
                        .setQuery(eventQuery, EventsList.class)
                        .build();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<EventsList, AdminEventViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final AdminEventViewHolder holder, int position, @NonNull final EventsList model) {
                        final String postKey = getRef(position).getKey();
                        final String uid = model.getUid();

                        holder.username.setText(model.getName());
                        holder.postdate.setText(model.getDate());
                        holder.des.setText(model.getDescription());
                        holder.posttime.setText(model.getTime());
                        holder.eventdate.setText("Date: " + model.getEventDate());
                        holder.eventCity.setText("Place: " + model.getCity());
                        holder.displayStatus.setText(model.getStatus());
                        Picasso.get().load(model.getPostImage()).into(holder.my_eventimage);
                        Picasso.get().load(model.getProfileImage()).into(holder.profileImage);
                        holder.setInterestButtonStatus(postKey);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent = new Intent(AdminEvents.this, FullEventActivity.class);
                                clickPostIntent.putExtra("PostKey", postKey);
                                startActivity(clickPostIntent);
                            }
                        });

                    }

                    @NonNull
                    @Override
                    public AdminEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_events_layout, parent, false);
                        AdminEventViewHolder viewHolder = new AdminEventViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class AdminEventViewHolder extends RecyclerView.ViewHolder{

        private TextView username, posttime, postdate, eventdate, eventCity, des, displayNoOfInterest, displayStatus;
        private ImageView my_eventimage;
        private CircleImageView profileImage;
        private int countInterest;
        private String cuurentUserId;
        private DatabaseReference interestRefrence;

        public AdminEventViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.admin_event_user_name);
            posttime = itemView.findViewById(R.id.admin_event_post_time);
            postdate = itemView.findViewById(R.id.admin_event_post_date);
            eventdate = itemView.findViewById(R.id.admin_event_date);
            eventCity = itemView.findViewById(R.id.admin_event_city);
            des = itemView.findViewById(R.id.admin_event_description);
            displayNoOfInterest = itemView.findViewById(R.id.admin_number_of_interest);
            displayStatus = itemView.findViewById(R.id.admin_event_status);
            my_eventimage = itemView.findViewById(R.id.admin_event_post_image);
            profileImage = itemView.findViewById(R.id.admin_event_profile_image);

            interestRefrence = FirebaseDatabase.getInstance().getReference().child("Interests");
            cuurentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setInterestButtonStatus(final String PostKey){
            interestRefrence.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.hasChild(PostKey)){
                        countInterest = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        displayNoOfInterest.setText((Integer.toString(countInterest) + " interested"));

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AdminEvents.this, AdminDashboard.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
