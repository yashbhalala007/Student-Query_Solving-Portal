package com.example.adlproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AdminComments extends AppCompatActivity {
    private RecyclerView commentRecyclerview;

    private DatabaseReference usersRef, backupRef;
    private FirebaseAuth mAuth;

    private FirebaseRecyclerAdapter<Comments, CommentActivity.commentsViewHolder> firebaseRecyclerAdapter;

    private String postKey, current_user_id, en_no;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_comments);

        postKey = getIntent().getExtras().get("Postkey").toString();
        en_no = getIntent().getExtras().get("en_no").toString();

        commentRecyclerview = findViewById(R.id.admin_com_recyclerView);
        commentRecyclerview.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentRecyclerview.setLayoutManager(linearLayoutManager);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        backupRef = FirebaseDatabase.getInstance().getReference().child("Backup").child("Posts").child(en_no).child(postKey).child("Comments");
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
    }

    @Override
    protected void onStart() {
        super.onStart();
        DisplayAllUserComments();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    private void DisplayAllUserComments() {
        FirebaseRecyclerOptions<Comments> options =
                new FirebaseRecyclerOptions.Builder<Comments>()
                        .setQuery(backupRef, Comments.class)
                        .build();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Comments, CommentActivity.commentsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull CommentActivity.commentsViewHolder holder, int position, @NonNull Comments model) {

                        holder.user_name.setText("@" + model.getUserName());
                        holder.date.setText(model.getDate());
                        holder.time.setText(model.getTime());
                        holder.comment_text.setText(model.getComment());
                    }

                    @NonNull
                    @Override
                    public CommentActivity.commentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_comments_layout, parent, false);
                        CommentActivity.commentsViewHolder viewHolder = new CommentActivity.commentsViewHolder(view);
                        return viewHolder;
                    }
                };
        commentRecyclerview.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }
}
