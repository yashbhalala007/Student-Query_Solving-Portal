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
import android.widget.EditText;
import android.widget.ImageButton;
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

public class CommentActivity extends AppCompatActivity {

    private ImageButton postCommentbtn;
    private EditText commentInput;
    private RecyclerView commentRecyclerview;

    private DatabaseReference usersRef, postRef, backupRef;
    private FirebaseAuth mAuth;

    private FirebaseRecyclerAdapter<Comments, commentsViewHolder> firebaseRecyclerAdapter;

    private String postKey, current_user_id, en_no;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        postKey = getIntent().getExtras().get("Postkey").toString();
        en_no = getIntent().getExtras().get("en_no").toString();

        commentRecyclerview = findViewById(R.id.com_recyclerView);
        commentRecyclerview.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentRecyclerview.setLayoutManager(linearLayoutManager);

        commentInput = findViewById(R.id.com_input);
        postCommentbtn = findViewById(R.id.com_post_btn);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey).child("Comments");
        backupRef = FirebaseDatabase.getInstance().getReference().child("Backup").child("Posts").child(en_no).child(postKey).child("Comments");
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        postCommentbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String userName = dataSnapshot.child("userName").getValue().toString();
                            validateComment(userName);
                            commentInput.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
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
                        .setQuery(postRef, Comments.class)
                        .build();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Comments, commentsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull commentsViewHolder holder, int position, @NonNull Comments model) {

                        holder.user_name.setText("@" + model.getUserName());
                        holder.date.setText(model.getDate());
                        holder.time.setText(model.getTime());
                        holder.comment_text.setText(model.getComment());
                    }

                    @NonNull
                    @Override
                    public commentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_comments_layout, parent, false);
                        CommentActivity.commentsViewHolder viewHolder = new CommentActivity.commentsViewHolder(view);
                        return viewHolder;
                    }
                };
        commentRecyclerview.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class commentsViewHolder extends RecyclerView.ViewHolder{
        TextView user_name, date, time, comment_text;

        public commentsViewHolder(@NonNull View itemView) {
            super(itemView);

            user_name = itemView.findViewById(R.id.comment_username);
            date = itemView.findViewById(R.id.comment_date);
            time = itemView.findViewById(R.id.comment_time);
            comment_text = itemView.findViewById(R.id.comment_text);
        }
    }

    private void validateComment(String userName) {
        String commnetText = commentInput.getText().toString();
        if(TextUtils.isEmpty(commnetText)){
            Toast.makeText(this,"Please, write something!!", Toast.LENGTH_LONG).show();
        }
        else {
            Calendar calDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveDate = currentDate.format(calDate.getTime());

            Calendar calTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveTime = currentTime.format(calTime.getTime());

            final String randomKey = current_user_id + saveDate + saveTime;

            HashMap commentsMap = new HashMap();
            commentsMap.put("uid", current_user_id);
            commentsMap.put("comment", commnetText);
            commentsMap.put("date", saveDate);
            commentsMap.put("time", saveTime);
            commentsMap.put("userName", userName);

            postRef.child(randomKey).updateChildren(commentsMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                Toast.makeText(CommentActivity.this,"You have commneted successfully", Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(CommentActivity.this,"Error!!!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

            backupRef.child(randomKey).updateChildren(commentsMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {

                        }
                    });
        }
    }
}
