package com.example.adlproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class Photos extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private Toolbar mToolbar;

    private FloatingActionButton floatingActionButton;

    private DatabaseReference usersRef, postRef, likesRef, backupRef;
    private FirebaseAuth mAuth;

    private String current_user_id, en_no;

    Boolean likeChecker = false;

    private FirebaseRecyclerAdapter<Posts, PostViewHolder> firebaseRecyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        mToolbar = findViewById(R.id.photos_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Photos");
        drawerLayout = findViewById(R.id.drawer_photos);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.imageButton);


        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        backupRef = FirebaseDatabase.getInstance().getReference().child("Backup").child("Posts");


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
        //set post view
        recyclerView = findViewById(R.id.all_users_photos_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Photos.this, AddPhotos.class);
                startActivity(intent);
            }
        });
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

        Query photoQuery = postRef.orderByChild("counter");

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(photoQuery, Posts.class)
                .build();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostViewHolder>(options)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull final PostViewHolder holder, int position, @NonNull final Posts model) {

                        final String postKey = getRef(position).getKey();
                        final String uid = model.getUid();

                        holder.username.setText(model.getName());
                        holder.postdate.setText(model.getDate());
                        holder.des.setText(model.getDescription());
                        holder.posttime.setText(model.getTime());
                        Picasso.get().load(model.getPostImage()).into(holder.my_postimage);
                        Picasso.get().load(model.getProfileImage()).into(holder.profileImage);
                        holder.setLikeButtonStatus(postKey);
                        holder.setCommentStatus(postKey);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent = new Intent(Photos.this, FullPostActivity.class);
                                clickPostIntent.putExtra("PostKey", postKey);
                                startActivity(clickPostIntent);
                            }
                        });


                        holder.menuButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PopupMenu popupMenu = new PopupMenu(getApplicationContext(), holder.menuButton);
                                if (!model.getUid().equals(current_user_id)){
                                    popupMenu.inflate(R.menu.other_user_post_menu);
                                }
                                else {
                                    popupMenu.inflate(R.menu.my_post_menu);
                                }
                                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {

                                        switch (item.getItemId()){
                                            case R.id.opm_view_photos:
                                            case R.id.mpm_view_photos:
                                                Intent i = new Intent(Photos.this, MyPhotos.class);
                                                i.putExtra("uid", uid);
                                                startActivity(i);
                                                return true;
                                            case R.id.opm_view_profile:
                                            case R.id.mpm_view_profile:
                                                Intent profileIntent = new Intent(Photos.this, Profile.class);
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
                                Intent commentPostIntent = new Intent(Photos.this, CommentActivity.class);
                                commentPostIntent.putExtra("Postkey", postKey);
                                commentPostIntent.putExtra("en_no", en_no);
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

                                        if (likeChecker.equals(true)){
                                            if (dataSnapshot.child(postKey).hasChild(current_user_id)){
                                                likesRef.child(postKey).child(current_user_id).removeValue();
                                                likeChecker = false;
                                            }
                                            else {
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
                    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_post_activity, parent, false);
                        PostViewHolder viewHolder = new PostViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void editPost(final String description, final String postKey) {
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
                                backupRef.child(en_no).child(postKey).child("description").setValue(editText.getText().toString());
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

    public static class PostViewHolder extends RecyclerView.ViewHolder{

        TextView username, posttime, postdate, des, displayNoOfLikes, displayNoOfComments;
        ImageView my_postimage;
        ImageButton likePostButton, commentPostButton, menuButton;
        CircleImageView profileImage;
        int countLikes, countComment;
        String cuurentUserId;
        DatabaseReference likesRefrence, commentRef;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.post_user_name);
            posttime = itemView.findViewById(R.id.post_time);
            postdate = itemView.findViewById(R.id.post_date);
            des = itemView.findViewById(R.id.post_descriprion);
            my_postimage = itemView.findViewById(R.id.post_image);
            displayNoOfLikes = itemView.findViewById(R.id.number_of_likes);
            displayNoOfComments = itemView.findViewById(R.id.number_of_comments);
            likePostButton = itemView.findViewById(R.id.like_button);
            commentPostButton = itemView.findViewById(R.id.comment_button);
            menuButton = itemView.findViewById(R.id.post_menu);
            profileImage = itemView.findViewById(R.id.post_profile_image);

            likesRefrence = FirebaseDatabase.getInstance().getReference().child("Likes");
            cuurentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setCommentStatus(final String Postkey){
            commentRef = FirebaseDatabase.getInstance().getReference().child("Posts");

            commentRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(Postkey).hasChild("Comments")){
                        countComment = (int) dataSnapshot.child(Postkey).child("Comments").getChildrenCount();
                        displayNoOfComments.setText((Integer.toString(countComment) + " comments"));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        public void setLikeButtonStatus(final String PostKey){
            likesRefrence.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.child(PostKey).hasChild(cuurentUserId)){
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.like);
                        displayNoOfLikes.setText((Integer.toString(countLikes) + " likes"));

                    }
                    else {
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.dislike);
                        displayNoOfLikes.setText((Integer.toString(countLikes) + " likes"));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Photos.this, Dashboard.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
