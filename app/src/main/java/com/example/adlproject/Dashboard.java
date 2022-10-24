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
import android.widget.ImageView;
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

public class Dashboard extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar mToolbar;
    private CircleImageView circleImageView;
    private TextView navUserName;
    private FloatingActionButton floatingActionButton;
    private EditText editText;

    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase, queRef;
    private String userid, currentDepartment, en_no, userRole;

    private FirebaseRecyclerAdapter<Questions, QuestionViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isConnected(Dashboard.this)){
            setContentView(R.layout.no_internet_connection);
            Log.i("MainActivity", "No Internet");
        }
        else
        {
            setContentView(R.layout.activity_dashboard);
            mAuth = FirebaseAuth.getInstance();

            userid = mAuth.getCurrentUser().getUid();
            mDatabase = FirebaseDatabase.getInstance().getReference();
            queRef = FirebaseDatabase.getInstance().getReference().child("Questions");

            mToolbar = findViewById(R.id.mainpage_toolbar);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setTitle("Home");
            drawerLayout = findViewById(R.id.drawer);
            actionBarDrawerToggle = new ActionBarDrawerToggle(Dashboard.this, drawerLayout,R.string.drawer_open, R.string.drawer_close);
            drawerLayout.addDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.syncState();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            navigationView = findViewById(R.id.navigation_menu);
            View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
            circleImageView = (CircleImageView) navView.findViewById(R.id.nav_prof_img);
            navUserName = (TextView) navView.findViewById(R.id.nav_username);
            floatingActionButton = (FloatingActionButton) findViewById(R.id.question_Button);
            editText = findViewById(R.id.dashboard_question_search);


            recyclerView = findViewById(R.id.all_users_que_list);
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);
            recyclerView.setLayoutManager(linearLayoutManager);

            mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){

                        Picasso.get().load(dataSnapshot.child("profileImage").getValue().toString()).into(circleImageView);
                        navUserName.setText("@" + dataSnapshot.child("userName").getValue().toString());
                        currentDepartment = dataSnapshot.child("department").getValue().toString();
                        en_no = dataSnapshot.child("enrolment_no").getValue().toString();
                        userRole = dataSnapshot.child("userRole").getValue().toString();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            loadingBar = new ProgressDialog(this);
            loadingBar.setTitle("Please Wait");
            loadingBar.setMessage("While account is getting ready");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    UserMenuSelector(menuItem);
                    return false;
                }
            });
        }

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent QuestionIntent = new Intent(Dashboard.this, AskQuestion.class);
                QuestionIntent.putExtra("Depart", currentDepartment);
                startActivity(QuestionIntent);
            }
        });

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
                    Query query = queRef.child(currentDepartment).orderByChild("counter");
                    DisplayAllUserQuestion(query);
                }else {
                    Query query = queRef.child(currentDepartment).orderByChild("question")
                            .startAt(search).endAt(search + "\uf8ff");
                    DisplayAllUserQuestion(query);
                }
            }
        });

    }

    public boolean isConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting())) return true;
        else return false;
        } else
        return false;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem menuItem) {

        switch (menuItem.getItemId()){

            case R.id.nva_addque:
                Intent QuestionIntent = new Intent(Dashboard.this, MyQuestionActivity.class);
                QuestionIntent.putExtra("Depart", currentDepartment);
                startActivity(QuestionIntent);
                break;
            case R.id.nva_event:
                Intent eventIntent = new Intent(Dashboard.this, Events.class);
                startActivity(eventIntent);
                break;
            case R.id.nva_add_user:
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.example.test");
                if (intent != null) {
                    // We found the activity now start the activity
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    // Bring user to the market or let them choose an app?
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse("market://details?id=" + "com.example.test"));
                    startActivity(intent);
                }
                break;
            case R.id.nva_logout:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(Dashboard.this, Login.class));
                break;
            case R.id.nva_photo:
                Intent photoIntent = new Intent(Dashboard.this, Photos.class);
                startActivity(photoIntent);
                break;
            case R.id.nva_profile:
                Intent profileIntent = new Intent(Dashboard.this, Profile.class);
                profileIntent.putExtra("userId", userid);
                profileIntent.putExtra("role", userRole);
                startActivity(profileIntent);
                break;
            case R.id.nva_ask_faculty:
                Intent askFintent = new Intent(Dashboard.this, FacultyHelp.class);
                askFintent.putExtra("Depart", currentDepartment);
                askFintent.putExtra("en_no", en_no);
                startActivity(askFintent);
                break;
            case R.id.nva_meeting_requests:
                startActivity(new Intent(Dashboard.this, FacultyMeetingRequests.class));
                break;
            case R.id.admin_event:
                startActivity(new Intent(Dashboard.this, AdminEvents.class));
                break;
            case R.id.admin_photo:
                startActivity(new Intent(Dashboard.this, AdminPhotos.class));
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isConnected(Dashboard.this)){
            //nothing
            Log.i("MainActivity", "No Internet");
        }else {

            mDatabase.child("Users").child(userid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (userRole.equals("Admin")) {
                        navigationView.inflateMenu(R.menu.navigation_menu_admin);
                    } else if (userRole.equals("Student")) {
                        navigationView.inflateMenu(R.menu.navigation_menu_student);
                    } else if (userRole.equals("Faculty")) {
                        navigationView.inflateMenu(R.menu.navigation_menu_faculty);
                    }
                    Query query = queRef.child(currentDepartment).orderByChild("counter");
                    DisplayAllUserQuestion(query);
                    loadingBar.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void DisplayAllUserQuestion(Query query) {

        FirebaseRecyclerOptions<Questions> options =
                new FirebaseRecyclerOptions.Builder<Questions>()
                        .setQuery(query, Questions.class)
                        .build();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Questions, QuestionViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull QuestionViewHolder holder, int position, @NonNull Questions model) {
                        final String postKey = getRef(position).getKey();
                        final String uid = model.getUid();
                        final String en_no = model.getEnrolment_no();
                        String q = model.getQuestion();
                        if (q.length() <= 200){
                            holder.que.setText(q);
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
                                Intent clickPostIntent = new Intent(Dashboard.this, FullQuestionActivity.class);
                                clickPostIntent.putExtra("PostKey", postKey);
                                clickPostIntent.putExtra("dept", currentDepartment);
                                startActivity(clickPostIntent);
                            }
                        });

                        holder.answer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent = new Intent(Dashboard.this, AnswerActivity.class);
                                clickPostIntent.putExtra("PostKey", postKey);
                                clickPostIntent.putExtra("Depart", currentDepartment);
                                clickPostIntent.putExtra("en_no", en_no);
                                startActivity(clickPostIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_user_question_layout, parent, false);
                        QuestionViewHolder viewHolder = new QuestionViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
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

    public static class QuestionViewHolder extends RecyclerView.ViewHolder{
        private TextView qdate, qtime, que, displayNoOfQue;
        private ImageButton answer;
        private DatabaseReference ansRef;
        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            qdate = itemView.findViewById(R.id.question_date);
            qtime = itemView.findViewById(R.id.question_time);
            que = itemView.findViewById(R.id.question_description);
            displayNoOfQue = itemView.findViewById(R.id.number_of_answer);
            answer = itemView.findViewById(R.id.answer_button);
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
