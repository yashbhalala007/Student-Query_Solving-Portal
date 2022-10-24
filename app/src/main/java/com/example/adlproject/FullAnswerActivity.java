package com.example.adlproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class FullAnswerActivity extends AppCompatActivity {
    private TextView textView;
    private Toolbar mToolbar;
    private RecyclerView images;

    private DatabaseReference queRef;

    private String postKey, dept, akey;

    private FirebaseRecyclerAdapter<Questions, AnswerImagesViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_answer);

        textView = findViewById(R.id.full_ans_description);

        images = findViewById(R.id.answer_image);

        mToolbar = findViewById(R.id.full_answer_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Full Answer");

        postKey = getIntent().getExtras().get("PostKey").toString();
        dept = getIntent().getExtras().get("dept").toString();
        akey = getIntent().getExtras().get("aKey").toString();

        queRef = FirebaseDatabase.getInstance().getReference().child("Questions");

        queRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    textView.setText(dataSnapshot.child(dept).child(postKey).child("Answers").child(akey).child("answer").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        images.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        images.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        DisplayAllImages();
    }

    private void DisplayAllImages() {

        Query query = queRef.child(dept).child(postKey).child("Answers").child(akey).child("Images");

        FirebaseRecyclerOptions<Questions> options =
                new FirebaseRecyclerOptions.Builder<Questions>()
                        .setQuery(query, Questions.class)
                        .build();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Questions, AnswerImagesViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull AnswerImagesViewHolder holder, int position, @NonNull Questions model) {
                        Picasso.get().load(model.getImage()).into(holder.imageView);
                    }

                    @NonNull
                    @Override
                    public AnswerImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_question_images_layout, parent, false);
                        AnswerImagesViewHolder viewHolder = new AnswerImagesViewHolder(view);
                        return viewHolder;
                    }
                };
        images.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class AnswerImagesViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        public AnswerImagesViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.all_que_image);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(FullAnswerActivity.this, FullQuestionActivity.class);
        intent.putExtra("PostKey", postKey);
        intent.putExtra("dept", dept);
        startActivity(intent);
    }
}
