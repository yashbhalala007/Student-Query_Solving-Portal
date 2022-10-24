package com.example.adlproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminQuestions extends AppCompatActivity {
    private RecyclerView recyclerView;
    private Toolbar mToolbar;
    private EditText editText;

    private DatabaseReference backupRef;
    private FirebaseAuth mAuth;

    private String userid;

    private FirebaseRecyclerAdapter<Questions, AdminQuestionViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_questions);
        mAuth = FirebaseAuth.getInstance();
        userid = mAuth.getCurrentUser().getUid();
        backupRef = FirebaseDatabase.getInstance().getReference().child("Backup").child("Questions");


        mToolbar = findViewById(R.id.admin_question_toolbar);
        editText = findViewById(R.id.admin_question_search);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Questions");

        recyclerView = findViewById(R.id.admin_question_list);
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
                    //firebaseRecyclerAdapter.stopListening();
                }
                else {
                    DisplayAllUserQuestion(search);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void DisplayAllUserQuestion(String search) {

        Query query = backupRef.child(search).orderByChild("counter");

        FirebaseRecyclerOptions<Questions> options =
                new FirebaseRecyclerOptions.Builder<Questions>()
                        .setQuery(query, Questions.class)
                        .build();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Questions, AdminQuestionViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull AdminQuestionViewHolder holder, int position, @NonNull Questions model) {
                        final String postKey = getRef(position).getKey();
                        final String uid = model.getUid();
                        final String en_no = model.getEnrolment_no();
                        String q = model.getQuestion();
                        //if (q.length() <= 200){
                            holder.que.setText(q);
                        //}
                        //else {
                          //  holder.que.setText(q.substring(0, 199) + "...");
                        //}
                        holder.qtime.setText(model.getTime());
                        holder.qdate.setText(model.getDate());
                        holder.setAnswerStatus(postKey, en_no);
                        holder.displayStatus.setText(model.getStatus());
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent = new Intent(AdminQuestions.this, AdminFullQuestionActivity.class);
                                clickPostIntent.putExtra("PostKey", postKey);
                                clickPostIntent.putExtra("en_no", en_no);
                                startActivity(clickPostIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public AdminQuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_question_list_layout, parent, false);
                        AdminQuestionViewHolder viewHolder = new AdminQuestionViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //firebaseRecyclerAdapter.stopListening();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AdminQuestions.this, AdminDashboard.class));
    }

    public static class AdminQuestionViewHolder extends RecyclerView.ViewHolder{
        private TextView qdate, qtime, que, displayNoOfQue, displayStatus;
        private DatabaseReference ansRef;
        public AdminQuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            qdate = itemView.findViewById(R.id.admin_question_date);
            qtime = itemView.findViewById(R.id.admin_question_time);
            que = itemView.findViewById(R.id.admin_question_description);
            displayNoOfQue = itemView.findViewById(R.id.admin_number_of_answer);
            displayStatus = itemView.findViewById(R.id.admin_que_status);
        }

        public void setAnswerStatus(String postKey, String en_no) {
            ansRef = FirebaseDatabase.getInstance().getReference().child("Backup").child("Questions").child(en_no).child(postKey);

            ansRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("Answers").exists()){
                        int countAnswer = (int) dataSnapshot.child("Answers").getChildrenCount();
                        displayNoOfQue.setText(Integer.toString(countAnswer) + " Answers");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
