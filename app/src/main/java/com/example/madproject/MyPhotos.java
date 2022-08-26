package com.example.madproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;

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

public class MyPhotos extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;

    private DatabaseReference usersRef, postRef, likesRef;
    private FirebaseAuth mAuth;

    private String current_user_id, userid;

    Boolean likeChecker = false;

    private FirebaseRecyclerAdapter<Posts, DashboardActivity.PostViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_photos);

        drawerLayout = findViewById(R.id.drawer_my_photos);
        mAuth = FirebaseAuth.getInstance();
        userid = mAuth.getCurrentUser().getUid();
        current_user_id = getIntent().getExtras().get("uid").toString();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        //set post view
        recyclerView = findViewById(R.id.my_photos_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        DisplayAllUserPost();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    private void DisplayAllUserPost() {

        Query query = postRef.orderByChild("uid")
                .startAt(current_user_id).endAt(current_user_id + "\uf8ff");

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(query, Posts.class)
                        .build();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, DashboardActivity.PostViewHolder>(options)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull final DashboardActivity.PostViewHolder holder, final int position, @NonNull final Posts model) {

                        final String postKey = getRef(position).getKey();
                        final String uid = model.getUid();

                        holder.username.setText(model.getName());
                        holder.postdate.setText(model.getDate());
                        holder.des.setText(model.getDescription());
                        holder.posttime.setText(model.getTime());
                        Picasso.get().load(model.getPostImage()).into(holder.my_postimage);
                        usersRef.child(uid)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild("profileImage")){
                                            Picasso.get().load(dataSnapshot.child("profileImage").getValue().toString()).into(holder.profileImage);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                        holder.setLikeButtonStatus(postKey);
                        holder.setCommentStatus(postKey);

                        holder.menuButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PopupMenu popupMenu = new PopupMenu(getApplicationContext(), holder.menuButton);
                                if (!model.getUid().equals(userid)) {
                                    popupMenu.inflate(R.menu.inside_photos_other);
                                } else {
                                    popupMenu.inflate(R.menu.i_in_photos);
                                }
                                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {

                                        switch (item.getItemId()) {
                                            case R.id.opm_view_profile:
                                            case R.id.mpm_view_profile:
                                                Intent profileIntent = new Intent(MyPhotos.this, Profile.class);
                                                profileIntent.putExtra("userId", model.getUid());
                                                startActivity(profileIntent);
                                                return true;
                                            case R.id.mpm_edit_post:
                                                editPost(model.getDescription(), postKey);
                                                return true;
                                            case R.id.mpm_delete_post:
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

                        holder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent commentPostIntent = new Intent(MyPhotos.this, CommentActivity.class);
                                commentPostIntent.putExtra("Postkey", postKey);
                                startActivity(commentPostIntent);
                            }
                        });

                        holder.likePostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                likeChecker = true;

                                likesRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (likeChecker.equals(true)) {
                                            if (dataSnapshot.child(postKey).hasChild(current_user_id)) {
                                                likesRef.child(postKey).child(current_user_id).removeValue();
                                                likeChecker = false;
                                            } else {
                                                likesRef.child(postKey).child(current_user_id).setValue(true);
                                                likeChecker = false;
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
                    public DashboardActivity.PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_post_activity, parent, false);
                        DashboardActivity.PostViewHolder viewHolder = new DashboardActivity.PostViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void editPost(String description, final String postKey) {
        AlertDialog.Builder Builder = new AlertDialog.Builder(this);
        Builder.setTitle("Edit Post");
        final EditText editText = new EditText(this);
        editText.setText(description);
        Builder.setView(editText);
        Builder.setCancelable(false)
                .setPositiveButton("UPDATE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // what to do if YES is tapped
                                postRef.child(postKey).child("description").setValue(editText.getText().toString());
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
                                postRef.child(postKey).removeValue();
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MyPhotos.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
