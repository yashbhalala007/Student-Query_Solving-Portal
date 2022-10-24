package com.example.adlproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jsibbold.zoomage.ZoomageView;
import com.squareup.picasso.Picasso;

public class FullEventActivity extends AppCompatActivity {

    private ZoomageView imageView;
    private TextView textView1, textView2, textView3;
    private String postKey;

    private DatabaseReference eventRef;
    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_event);

        imageView = findViewById(R.id.full_event_image);
        textView1 = findViewById(R.id.full_event_city);
        textView2 = findViewById(R.id.full_event_date);
        textView3 = findViewById(R.id.full_event_description);

        postKey = getIntent().getExtras().get("PostKey").toString();

        eventRef = FirebaseDatabase.getInstance().getReference().child("Events").child(postKey);
        loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Event");
        loadingBar.setMessage("Event is fetching");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);

        eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    textView1.setText(dataSnapshot.child("city").getValue().toString());
                    textView2.setText(dataSnapshot.child("eventDate").getValue().toString());
                    textView3.setText(dataSnapshot.child("description").getValue().toString());
                    Picasso.get().load(dataSnapshot.child("postImage").getValue().toString()).into(imageView);
                    loadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
