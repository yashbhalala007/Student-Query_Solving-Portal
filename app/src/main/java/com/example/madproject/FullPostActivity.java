package com.example.madproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jsibbold.zoomage.ZoomageView;
import com.squareup.picasso.Picasso;

public class FullPostActivity extends AppCompatActivity {

    private ZoomageView imageView;
    private TextView textView;
    private String postKey;

    private DatabaseReference postRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_post);

        imageView = (ZoomageView) findViewById(R.id.full_post_image);
        textView = findViewById(R.id.full_post_description);

        postKey = getIntent().getExtras().get("PostKey").toString();

        postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);
        loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Post");
        loadingBar.setMessage("Post is fetching");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);

        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    textView.setText(dataSnapshot.child("description").getValue().toString());
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
