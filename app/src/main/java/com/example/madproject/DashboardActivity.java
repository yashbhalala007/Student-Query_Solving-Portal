package com.example.madproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class DashboardActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar mToolbar;
    private CircleImageView circleImageView;
    private TextView navUserName;

    private FloatingActionButton floatingActionButton;

    private DatabaseReference usersRef, postRef, likesRef;
    private FirebaseAuth mAuth;

    private String current_user_id;

    Boolean likeChecker = false;

    private FirebaseRecyclerAdapter<Posts, PostViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mToolbar = findViewById(R.id.photos_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");
        drawerLayout = findViewById(R.id.drawer);
        actionBarDrawerToggle = new ActionBarDrawerToggle(DashboardActivity.this, drawerLayout,R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = findViewById(R.id.navigation_menu);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        circleImageView = (CircleImageView) navView.findViewById(R.id.nav_prof_img);
        navUserName = (TextView) navView.findViewById(R.id.nav_username);


        floatingActionButton = (FloatingActionButton) findViewById(R.id.photosButton);
        navigationView.inflateMenu(R.menu.dashboard_menu);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        //set post view
        recyclerView = findViewById(R.id.all_users_photos_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.hasChild("profileImage")) {
                        Picasso.get().load(dataSnapshot.child("profileImage").getValue().toString()).into(circleImageView);
                    }
                    navUserName.setText(dataSnapshot.child("name").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, AddPhotos.class);
                startActivity(intent);
            }
        });


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                userMenuSelector(item);
                return false;
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        DisplayAllUserPost();
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

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent = new Intent(DashboardActivity.this, FullPostActivity.class);
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
                                                Intent i = new Intent(DashboardActivity.this, MyPhotos.class);
                                                i.putExtra("uid", uid);
                                                startActivity(i);
                                                return true;
                                            case R.id.opm_view_profile:
                                            case R.id.mpm_view_profile:
                                                Intent profileIntent = new Intent(DashboardActivity.this, Profile.class);
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
                                Intent commentPostIntent = new Intent(DashboardActivity.this, CommentActivity.class);
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
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    private void userMenuSelector(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_profile:
                Intent profileIntent = new Intent(DashboardActivity.this, Profile.class);
                profileIntent.putExtra("userId", current_user_id);
                startActivity(profileIntent);
                break;
            case R.id.menu_logout:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(DashboardActivity.this, MainActivity.class));
                break;
            case R.id.menu_add_photo:
                Intent intent = new Intent(DashboardActivity.this, AddPhotos.class);
                startActivity(intent);
                break;
            case R.id.menu_my_photos:
                Intent eventIntent = new Intent(DashboardActivity.this, MyPhotos.class);
                eventIntent.putExtra("uid", current_user_id);
                startActivity(eventIntent);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Warning");
        alertDialogBuilder
                .setMessage("Do you really want to exit?")
                .setCancelable(false)
                .setPositiveButton("EXIT",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // what to do if YES is tapped
                                finishAffinity();
                                System.exit(0);
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


}
