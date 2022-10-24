package com.example.adlproject;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;


public class FacultyHelp extends AppCompatActivity {

    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPager viewPager;

    private DatabaseReference userRef;
    private FirebaseAuth mAuth;

    private String current_user_id, role;
    private String currentDepartment, en_no;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_help);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        en_no = getIntent().getExtras().get("en_no").toString();
        currentDepartment = getIntent().getExtras().get("Depart").toString();
        userRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    role = dataSnapshot.child("userRole").getValue().toString();

                    Bundle bundle = new Bundle();
                    bundle.putString("role", role);
                    bundle.putString("en_no", en_no);
                    Fragment facultyPendingRequest = new FacultyPendingRequest();
                    facultyPendingRequest.setArguments(bundle);

                    Bundle bundle1 = new Bundle();
                    bundle1.putString("role", role);
                    bundle1.putString("en_no", en_no);
                    Fragment facultyConfirmRequest = new FacultyConfirmRequest();
                    facultyConfirmRequest.setArguments(bundle1);

                    Bundle bundle2 = new Bundle();
                    bundle2.putString("role", role);
                    bundle2.putString("en_no", en_no);
                    Fragment facultyRejectedRequest = new FacultyRejectedRequest();
                    facultyRejectedRequest.setArguments(bundle2);

                    viewPager = (ViewPager) findViewById(R.id.view_pager);
                    setupViewPager(viewPager, facultyConfirmRequest, facultyPendingRequest, facultyRejectedRequest);
                    TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
                    tabLayout.setupWithViewPager(viewPager);
                    if (role.equals("Admin")){
                        FloatingActionButton fab = findViewById(R.id.fab);
                        fab.hide();
                    }
                    else {
                        FloatingActionButton fab = findViewById(R.id.fab);

                        fab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent askFacultyIntent = new Intent(FacultyHelp.this, AskFaculty.class);
                                askFacultyIntent.putExtra("Depart", currentDepartment);
                                startActivity(askFacultyIntent);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupViewPager(ViewPager viewPager, Fragment facultyConfirmRequest, Fragment facultyPendingRequest, Fragment facultyRejectedRequest) {

        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(facultyConfirmRequest, "Confirmed");
        adapter.addFragment(facultyPendingRequest, "Pending");
        adapter.addFragment(facultyRejectedRequest, "Rejected");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        if (role.equals("Admin")) {
            Intent intent = new Intent(FacultyHelp.this, AdminMeetings.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }else {
            Intent intent = new Intent(FacultyHelp.this, Dashboard.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}