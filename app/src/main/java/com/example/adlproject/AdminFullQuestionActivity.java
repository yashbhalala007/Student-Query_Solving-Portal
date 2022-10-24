package com.example.adlproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class AdminFullQuestionActivity extends AppCompatActivity {
    private TextView textView, textView2;
    private Toolbar mToolbar;
    private RecyclerView images, answers;

    private DatabaseReference backupRef;

    private String postKey, en_no;

    private FirebaseRecyclerAdapter<Questions, FullQuestionActivity.ImagesViewHolder> firebaseRecyclerAdapter;

    private FirebaseRecyclerAdapter<Questions, FullQuestionActivity.AnswersViewHolder> firebaseRecyclerAdapter2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_full_question);

        textView = findViewById(R.id.admin_full_que_description);
        textView2 = findViewById(R.id.admin_full_number_of_answer);

        images = findViewById(R.id.admin_question_image);
        answers = findViewById(R.id.admin_question_answer);

        mToolbar = findViewById(R.id.admin_full_question_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Full Question");

        postKey = getIntent().getExtras().get("PostKey").toString();
        en_no = getIntent().getExtras().get("en_no").toString();

        backupRef = FirebaseDatabase.getInstance().getReference().child("Backup").child("Questions").child(en_no).child(postKey);

        backupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    textView.setText(dataSnapshot.child("question").getValue().toString());
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

        answers.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this);
        linearLayoutManager1.setReverseLayout(true);
        linearLayoutManager1.setStackFromEnd(true);
        answers.setLayoutManager(linearLayoutManager1);
        setAnswerStatus(postKey);
    }

    private void setAnswerStatus(String postKey) {
        backupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("Answers").exists()){
                    int countAnswer = (int) dataSnapshot.child("Answers").getChildrenCount();
                    textView2.setText(Integer.toString(countAnswer) + " Answers");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        DisplayAllImages();
        DisplayAllAnswers();
    }

    private void DisplayAllAnswers() {
        FirebaseRecyclerOptions<Questions> options =
                new FirebaseRecyclerOptions.Builder<Questions>()
                        .setQuery(backupRef.child("Answers"), Questions.class)
                        .build();

        firebaseRecyclerAdapter2 =
                new FirebaseRecyclerAdapter<Questions, FullQuestionActivity.AnswersViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FullQuestionActivity.AnswersViewHolder holder, int position, @NonNull Questions model) {
                        final String pk = getRef(position).getKey();
                        holder.atime.setText(model.getTime());
                        holder.adate.setText(model.getDate());
                        holder.ans.setText(model.getAnswer());

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent = new Intent(AdminFullQuestionActivity.this, AdminFullAnswerActivity.class);
                                clickPostIntent.putExtra("PostKey", postKey);
                                clickPostIntent.putExtra("en_no", en_no);
                                clickPostIntent.putExtra("aKey", pk);
                                startActivity(clickPostIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FullQuestionActivity.AnswersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_answer_layout, parent, false);
                        FullQuestionActivity.AnswersViewHolder viewHolder = new FullQuestionActivity.AnswersViewHolder(view);
                        return viewHolder;
                    }
                };
        answers.setAdapter(firebaseRecyclerAdapter2);
        firebaseRecyclerAdapter2.startListening();
    }

    private void DisplayAllImages() {
        FirebaseRecyclerOptions<Questions> options =
                new FirebaseRecyclerOptions.Builder<Questions>()
                        .setQuery(backupRef.child("Images"), Questions.class)
                        .build();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Questions, FullQuestionActivity.ImagesViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FullQuestionActivity.ImagesViewHolder holder, int position, @NonNull Questions model) {
                        Picasso.get().load(model.getImage()).into(holder.imageView);
                    }

                    @NonNull
                    @Override
                    public FullQuestionActivity.ImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_question_images_layout, parent, false);
                        FullQuestionActivity.ImagesViewHolder viewHolder = new FullQuestionActivity.ImagesViewHolder(view);
                        return viewHolder;
                    }
                };
        images.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AdminFullQuestionActivity.this, AdminQuestions.class));
    }
}
