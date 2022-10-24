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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class FullQuestionActivity extends AppCompatActivity {

    private TextView textView, textView2;
    private Toolbar mToolbar;
    private RecyclerView images, answers;
    private ImageButton imageButton;

    private DatabaseReference queRef;

    private String postKey, dept;

    private FirebaseRecyclerAdapter<Questions, ImagesViewHolder> firebaseRecyclerAdapter;

    private FirebaseRecyclerAdapter<Questions, AnswersViewHolder> firebaseRecyclerAdapter2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_question);

        textView = findViewById(R.id.full_que_description);
        textView2 = findViewById(R.id.full_number_of_answer);
        imageButton = findViewById(R.id.full_answer_button);

        images = findViewById(R.id.question_image);
        answers = findViewById(R.id.question_answer);

        mToolbar = findViewById(R.id.full_question_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Full Question");

        postKey = getIntent().getExtras().get("PostKey").toString();
        dept = getIntent().getExtras().get("dept").toString();

        queRef = FirebaseDatabase.getInstance().getReference().child("Questions").child(dept).child(postKey);

        queRef.addValueEventListener(new ValueEventListener() {
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

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent clickPostIntent = new Intent(FullQuestionActivity.this, AnswerActivity.class);
                clickPostIntent.putExtra("PostKey", postKey);
                clickPostIntent.putExtra("Depart", dept);
                startActivity(clickPostIntent);
            }
        });
        setAnswerStatus(postKey);
    }

    private void setAnswerStatus(String postKey) {
        queRef.addValueEventListener(new ValueEventListener() {
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
                        .setQuery(queRef.child("Answers"), Questions.class)
                        .build();

        firebaseRecyclerAdapter2 =
                new FirebaseRecyclerAdapter<Questions, AnswersViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull AnswersViewHolder holder, int position, @NonNull Questions model) {
                        final String pk = getRef(position).getKey();
                        holder.atime.setText(model.getTime());
                        holder.adate.setText(model.getDate());
                        holder.ans.setText(model.getAnswer());

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent = new Intent(FullQuestionActivity.this, FullAnswerActivity.class);
                                clickPostIntent.putExtra("PostKey", postKey);
                                clickPostIntent.putExtra("dept", dept);
                                clickPostIntent.putExtra("aKey", pk);
                                startActivity(clickPostIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public AnswersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_answer_layout, parent, false);
                        AnswersViewHolder viewHolder = new AnswersViewHolder(view);
                        return viewHolder;
                    }
                };
        answers.setAdapter(firebaseRecyclerAdapter2);
        firebaseRecyclerAdapter2.startListening();
    }

    private void DisplayAllImages() {
        FirebaseRecyclerOptions<Questions> options =
                new FirebaseRecyclerOptions.Builder<Questions>()
                        .setQuery(queRef.child("Images"), Questions.class)
                        .build();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Questions, ImagesViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ImagesViewHolder holder, int position, @NonNull Questions model) {
                        Picasso.get().load(model.getImage()).into(holder.imageView);
                    }

                    @NonNull
                    @Override
                    public ImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_question_images_layout, parent, false);
                        ImagesViewHolder viewHolder = new ImagesViewHolder(view);
                        return viewHolder;
                    }
                };
        images.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class ImagesViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        public ImagesViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.all_que_image);
        }
    }

    public static class AnswersViewHolder extends RecyclerView.ViewHolder{
        TextView adate, atime, ans;
        public AnswersViewHolder(@NonNull View itemView) {
            super(itemView);

            adate = itemView.findViewById(R.id.ans_date);
            atime = itemView.findViewById(R.id.ans_time);
            ans = itemView.findViewById(R.id.ans_description);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(FullQuestionActivity.this, Dashboard.class);
        startActivity(intent);
    }
}
