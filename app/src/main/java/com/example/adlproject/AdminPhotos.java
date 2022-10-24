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
import android.widget.ImageButton;
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

public class AdminPhotos extends AppCompatActivity {
    private RecyclerView recyclerView;
    private Toolbar mToolbar;
    private EditText editText;

    private DatabaseReference backupRef;
    private FirebaseAuth mAuth;

    private String current_user_id;

    private FirebaseRecyclerAdapter<Posts, AdminPostViewHolder> firebaseRecyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_photos);

        mToolbar = findViewById(R.id.admin_photos_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Photos");
        editText = findViewById(R.id.admin_photos_search);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
        backupRef = FirebaseDatabase.getInstance().getReference().child("Backup").child("Posts");

        //set post view
        recyclerView = findViewById(R.id.admin_photos_list);
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
                    DisplayAllUserPost(search);
                }
            }
        });
    }

    private void DisplayAllUserPost(final String search) {

        Query photoQuery = backupRef.child(search).orderByChild("counter");

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(photoQuery, Posts.class)
                        .build();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, AdminPostViewHolder>(options)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull final AdminPostViewHolder holder, int position, @NonNull final Posts model) {

                        final String postKey = getRef(position).getKey();
                        final String uid = model.getUid();

                        holder.username.setText(model.getName());
                        holder.postdate.setText(model.getDate());
                        holder.des.setText(model.getDescription());
                        holder.posttime.setText(model.getTime());
                        Picasso.get().load(model.getPostImage()).into(holder.my_postimage);
                        Picasso.get().load(model.getProfileImage()).into(holder.profileImage);
                        holder.displayStatus.setText(model.getStatus());
                        holder.setLikeButtonStatus(postKey);
                        holder.setCommentStatus(postKey);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent = new Intent(AdminPhotos.this, FullPostActivity.class);
                                clickPostIntent.putExtra("PostKey", postKey);
                                startActivity(clickPostIntent);
                            }
                        });

                        holder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent commentPostIntent = new Intent(AdminPhotos.this, AdminComments.class);
                                commentPostIntent.putExtra("Postkey", postKey);
                                commentPostIntent.putExtra("en_no", search);
                                startActivity(commentPostIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public AdminPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_photos_layout, parent, false);
                        AdminPostViewHolder viewHolder = new AdminPostViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class AdminPostViewHolder extends RecyclerView.ViewHolder{

        private TextView username, posttime, postdate, des, displayNoOfLikes, displayNoOfComments, displayStatus;
        private ImageView my_postimage;
        private ImageButton commentPostButton;
        private CircleImageView profileImage;
        private int countLikes, countComment;
        private String cuurentUserId;
        private DatabaseReference likesRefrence, commentRef;

        public AdminPostViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.admin_post_user_name);
            posttime = itemView.findViewById(R.id.admin_post_time);
            postdate = itemView.findViewById(R.id.admin_post_date);
            des = itemView.findViewById(R.id.admin_post_descriprion);
            my_postimage = itemView.findViewById(R.id.admin_post_image);
            displayNoOfLikes = itemView.findViewById(R.id.admin_number_of_likes);
            displayNoOfComments = itemView.findViewById(R.id.admin_number_of_comments);
            commentPostButton = itemView.findViewById(R.id.admin_comment_button);
            profileImage = itemView.findViewById(R.id.admin_post_profile_image);
            displayStatus = itemView.findViewById(R.id.admin_post_status);

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

                    if(dataSnapshot.hasChild(PostKey)){
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
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
        Intent intent = new Intent(AdminPhotos.this, AdminDashboard.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
