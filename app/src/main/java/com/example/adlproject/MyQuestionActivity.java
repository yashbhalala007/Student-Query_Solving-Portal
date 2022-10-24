package com.example.adlproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
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

public class MyQuestionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase, queRef, backupRef;
    private String userid, currentDepartment;

    private FirebaseRecyclerAdapter<Questions, MyQuestionViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_question);
        mAuth = FirebaseAuth.getInstance();

        userid = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        queRef = FirebaseDatabase.getInstance().getReference().child("Questions");
        backupRef = FirebaseDatabase.getInstance().getReference().child("Backup").child("Questions");

        mToolbar = findViewById(R.id.my_que_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Questions");

        currentDepartment = getIntent().getExtras().get("Depart").toString();

        recyclerView = findViewById(R.id.my_que_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        DisplayAllUserQuestion();
    }

    private void DisplayAllUserQuestion() {

        Query query = queRef.child(currentDepartment).orderByChild("uid")
                .startAt(userid).endAt(userid + "\uf8ff");

        FirebaseRecyclerOptions<Questions> options =
                new FirebaseRecyclerOptions.Builder<Questions>()
                        .setQuery(query, Questions.class)
                        .build();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Questions, MyQuestionViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final MyQuestionViewHolder holder, int position, @NonNull final Questions model) {
                        final String postKey = getRef(position).getKey();
                        final String en_no = model.getEnrolment_no();
                        String q = model.getQuestion();
                        if (q.length() <= 200){
                            holder.que.setText(model.getQuestion());
                        }
                        else {
                            holder.que.setText(q.substring(0, 199) + "...");
                        }
                        holder.qtime.setText(model.getTime());
                        holder.qdate.setText(model.getDate());
                        holder.setAnswerStatus(postKey, currentDepartment);
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent = new Intent(MyQuestionActivity.this, FullQuestionActivity.class);
                                clickPostIntent.putExtra("PostKey", postKey);
                                clickPostIntent.putExtra("dept", currentDepartment);
                                startActivity(clickPostIntent);
                            }
                        });

                        holder.answer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent = new Intent(MyQuestionActivity.this, AnswerActivity.class);
                                clickPostIntent.putExtra("PostKey", postKey);
                                clickPostIntent.putExtra("Depart", currentDepartment);
                                clickPostIntent.putExtra("en_no", en_no);
                                startActivity(clickPostIntent);
                            }
                        });
                        holder.menu.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PopupMenu popupMenu = new PopupMenu(getApplicationContext(), holder.menu);
                                popupMenu.inflate(R.menu.question_menu);
                                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        switch (item.getItemId()){
                                            case R.id.menu_edit_question:
                                                editQuestion(model.getQuestion(), postKey, en_no);
                                                return true;
                                            case R.id.menu_delete_question:
                                                deleteQuestion(postKey, en_no);
                                                return true;
                                            default:
                                                return false;
                                        }
                                    }
                                });
                                popupMenu.show();
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public MyQuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_question_layout, parent, false);
                        MyQuestionViewHolder viewHolder = new MyQuestionViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void editQuestion(String question, final String postKey, final String en_no) {
        AlertDialog.Builder Builder = new AlertDialog.Builder(this);
        Builder.setTitle("Edit Question");
        final EditText editText = new EditText(this);
        editText.setText(question);
        Builder.setView(editText);
        Builder.setCancelable(false)
                .setPositiveButton("UPDATE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // what to do if YES is tapped
                                queRef.child(currentDepartment).child(postKey).child("question").setValue(editText.getText().toString());
                                backupRef.child(en_no).child(postKey).child("question").setValue(editText.getText().toString());
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

    private void deleteQuestion(final String postKey, final String en_no) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Warning");
        alertDialogBuilder
                .setMessage("Do you really want to delete question?")
                .setCancelable(false)
                .setPositiveButton("DELETE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // what to do if YES is tapped
                                queRef.child(currentDepartment).child(postKey).removeValue();
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

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(MyQuestionActivity.this, Dashboard.class));
    }

    public static class MyQuestionViewHolder extends RecyclerView.ViewHolder{
        private TextView qdate, qtime, que, displayNoOfQue;
        private ImageButton answer, menu;
        private DatabaseReference ansRef;
        public MyQuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            qdate = itemView.findViewById(R.id.my_ans_date);
            qtime = itemView.findViewById(R.id.my_ans_time);
            que = itemView.findViewById(R.id.my_ans_description);
            displayNoOfQue = itemView.findViewById(R.id.number_of_my_ans);
            answer = itemView.findViewById(R.id.my_ans_button);
            menu = itemView.findViewById(R.id.my_ans_menu);
        }

        public void setAnswerStatus(String postKey, String currentDepartment) {
            ansRef = FirebaseDatabase.getInstance().getReference().child("Questions").child(currentDepartment).child(postKey);

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
