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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

public class Events extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Toolbar mToolbar;

    private FloatingActionButton floatingActionButton;

    private DatabaseReference usersRef, eventRef, interestRef, backupRef;
    private FirebaseAuth mAuth;

    private String current_user_id, en_no;

    private Boolean interestChecker = false;

    private FirebaseRecyclerAdapter<EventsList, EventViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        mToolbar = findViewById(R.id.events_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Events");
        floatingActionButton = (FloatingActionButton) findViewById(R.id.eventsButton);


        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        eventRef = FirebaseDatabase.getInstance().getReference().child("Events");
        interestRef = FirebaseDatabase.getInstance().getReference().child("Interests");
        backupRef = FirebaseDatabase.getInstance().getReference().child("Backup").child("Events");

        //set post view
        recyclerView = findViewById(R.id.all_users_event_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    en_no = dataSnapshot.child("enrolment_no").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Events.this, AddEvents.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayAllUserEvents();
    }

    private void displayAllUserEvents() {

        Query eventQuery = eventRef.orderByChild("counter");

        FirebaseRecyclerOptions<EventsList> options =
                new FirebaseRecyclerOptions.Builder<EventsList>()
                        .setQuery(eventQuery, EventsList.class)
                        .build();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<EventsList, EventViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final EventViewHolder holder, int position, @NonNull final EventsList model) {
                        final String postKey = getRef(position).getKey();
                        final String uid = model.getUid();

                        holder.username.setText(model.getName());
                        holder.postdate.setText(model.getDate());
                        holder.des.setText(model.getDescription());
                        holder.posttime.setText(model.getTime());
                        holder.eventdate.setText("Date: " + model.getEventDate());
                        holder.eventCity.setText("Place: " + model.getCity());
                        Picasso.get().load(model.getPostImage()).into(holder.my_eventimage);
                        Picasso.get().load(model.getProfileImage()).into(holder.profileImage);
                        holder.setInterestButtonStatus(postKey);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent = new Intent(Events.this, FullEventActivity.class);
                                clickPostIntent.putExtra("PostKey", postKey);
                                startActivity(clickPostIntent);
                            }
                        });

                        holder.menuButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PopupMenu popupMenu = new PopupMenu(getApplicationContext(), holder.menuButton);
                                if (!model.getUid().equals(current_user_id)){
                                    popupMenu.inflate(R.menu.other_event_menu);
                                }
                                else {
                                    popupMenu.inflate(R.menu.my_event_menu);
                                }
                                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {

                                        switch (item.getItemId()){
                                            case R.id.oem_view_photos:
                                            case R.id.mem_view_photos:
                                                Intent i = new Intent(Events.this, MyEvents.class);
                                                i.putExtra("uid", uid);
                                                startActivity(i);
                                                return true;
                                            case R.id.oem_view_profile:
                                            case R.id.mem_view_profile:
                                                Intent profileIntent = new Intent(Events.this, Profile.class);
                                                profileIntent.putExtra("userId", model.getUid());
                                                startActivity(profileIntent);
                                                return true;
                                            case R.id.mem_edit_post:
                                                editPost(model.getDescription(), postKey, model.getCity());
                                                return true;
                                            case R.id.mem_delete_post:
                                                deletePost(postKey);
                                                return true;
                                            default:
                                                return false;
                                        }

                                    }
                                });
                                popupMenu.show();
                            }
                        });

                        holder.interestPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                interestChecker = true;

                                interestRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (interestChecker.equals(true)){
                                            if (dataSnapshot.child(postKey).hasChild(current_user_id)){
                                                interestRef.child(postKey).child(current_user_id).removeValue();
                                                interestChecker = false;
                                            }
                                            else {
                                                interestRef.child(postKey).child(current_user_id).setValue(true);
                                                interestChecker = false;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_event_activity, parent, false);
                        EventViewHolder viewHolder = new EventViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void deletePost(final String postKey) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Warning");
        alertDialogBuilder
                .setMessage("Do you really want to delete post?")
                .setCancelable(false)
                .setPositiveButton("DELETE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // what to do if YES is tapped
                                eventRef.child(postKey).removeValue();
                                backupRef.child(en_no).child(postKey).child("status").setValue("Deleted");
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

    private void editPost(String description, final String postKey, String city) {
        AlertDialog.Builder Builder = new AlertDialog.Builder(this);
        Builder.setTitle("Edit Post");

        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        final TextView textView1 = new TextView(this);
        textView1.setText("Description");
        textView1.setTextSize(14);
        textView1.setLayoutParams(params);
        linearLayout.addView(textView1);
        final EditText editText = new EditText(this);
        editText.setText(description);
        editText.setLayoutParams(params);
        linearLayout.addView(editText);
        final TextView textView2 = new TextView(this);
        textView2.setText("City");
        textView2.setTextSize(14);
        textView2.setLayoutParams(params);
        linearLayout.addView(textView2);
        final EditText editText1 = new EditText(this);
        editText1.setText(city);
        editText1.setLayoutParams(params);
        linearLayout.addView(editText1);
        final TextView textView = new TextView(this);
        textView.setText("Date");
        textView.setTextSize(14);
        textView.setLayoutParams(params);
        linearLayout.addView(textView);
        final ImageButton imageButton = new ImageButton(this);
        imageButton.setBackground(getDrawable(R.drawable.calendar));
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        imageButton.setLayoutParams(params1);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(v, textView);
            }
        });
        linearLayout.addView(imageButton);
        Builder.setView(linearLayout);

        Builder.setCancelable(false)
                .setPositiveButton("UPDATE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // what to do if YES is tapped
                                eventRef.child(postKey).child("description").setValue(editText.getText().toString());
                                eventRef.child(postKey).child("city").setValue(editText1.getText().toString());
                                eventRef.child(postKey).child("eventDate").setValue(textView.getText().toString());
                                backupRef.child(en_no).child(postKey).child("description").setValue(editText.getText().toString());
                                backupRef.child(en_no).child(postKey).child("city").setValue(editText1.getText().toString());
                                backupRef.child(en_no).child(postKey).child("eventDate").setValue(textView.getText().toString());
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
        Dialog dialog = Builder.create();
        dialog.show();

    }

    private void showDatePicker(View v, TextView textView) {
        DialogFragment newFragment = new AddEvents.MyDatePickerFragment(textView);
        newFragment.show(getSupportFragmentManager(), "date picker");
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder{

        TextView username, posttime, postdate, eventdate, eventCity, des, displayNoOfInterest, displayStatus;
        ImageView my_eventimage;
        ImageButton interestPostButton, menuButton;
        CircleImageView profileImage;
        int countInterest;
        String cuurentUserId;
        DatabaseReference interestRefrence;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.event_user_name);
            posttime = itemView.findViewById(R.id.event_post_time);
            postdate = itemView.findViewById(R.id.event_post_date);
            eventdate = itemView.findViewById(R.id.event_date);
            eventCity = itemView.findViewById(R.id.event_city);
            des = itemView.findViewById(R.id.event_descriprion);
            displayNoOfInterest = itemView.findViewById(R.id.number_of_interest);
            displayStatus = itemView.findViewById(R.id.event_interest_type);
            my_eventimage = itemView.findViewById(R.id.event_post_image);
            interestPostButton = itemView.findViewById(R.id.event_interest_button);
            profileImage = itemView.findViewById(R.id.event_profile_image);
            menuButton = itemView.findViewById(R.id.event_menu);

            interestRefrence = FirebaseDatabase.getInstance().getReference().child("Interests");
            cuurentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setInterestButtonStatus(final String PostKey){
            interestRefrence.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.child(PostKey).hasChild(cuurentUserId)){
                        countInterest = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        interestPostButton.setImageResource(R.drawable.interested);
                        displayNoOfInterest.setText((Integer.toString(countInterest) + " interested"));
                        displayStatus.setText("Interested");

                    }
                    else {
                        countInterest = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        interestPostButton.setImageResource(R.drawable.thumb);
                        displayNoOfInterest.setText((Integer.toString(countInterest) + " interested"));
                        displayStatus.setText("Not Interested");
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
        firebaseRecyclerAdapter.stopListening();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Events.this, Dashboard.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
