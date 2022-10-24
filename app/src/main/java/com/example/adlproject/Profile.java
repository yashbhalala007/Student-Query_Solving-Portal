package com.example.adlproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    private TextView txt1, txt2, txt3, txt4, txt5, txt6;
    private CircleImageView circleImageView;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference userProfileRef;
    private String userid, downloadUrl, role;

    private ProgressDialog loadingBar;
    final static int Gallery_Pick = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        txt1 = findViewById(R.id.my_profile_name);
        txt2 = findViewById(R.id.my_user_name);
        txt3 = findViewById(R.id.my_enrollment);
        txt4 = findViewById(R.id.my_email);
        txt5 = findViewById(R.id.my_department);
        txt6 = findViewById(R.id.my_gender);
        circleImageView = findViewById(R.id.my_profile_pic);

        mAuth = FirebaseAuth.getInstance();
        userid = getIntent().getExtras().get("userId").toString();
        role = getIntent().getExtras().get("role").toString();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userProfileRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Wait");
        loadingBar.setMessage("Profile is fetching");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (userid.equals(mAuth.getUid())) {
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent, Gallery_Pick);
                }
            }
        });

        mDatabase.child("Users").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String image = dataSnapshot.child("profileImage").getValue().toString();
                    Picasso.get().load(image).into(circleImageView);
                    loadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK){
                Uri resultUri = result.getUri();
                loadingBar = new ProgressDialog(this);
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Profile image is updating");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);
                final StorageReference filepath = userProfileRef.child(mAuth.getUid() + ".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(Profile.this, "Success", Toast.LENGTH_LONG).show();
                            filepath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    downloadUrl = task.getResult().toString();
                                    mDatabase.child("Users").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            dataSnapshot.getRef().child("profileImage").setValue(downloadUrl);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
            else {
                Toast.makeText(Profile.this,"Fail", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDatabase.child("Users").child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                txt2.setText("@" + (String) dataSnapshot.child("userName").getValue());
                txt1.setText((String) dataSnapshot.child("name").getValue());
                txt3.setText("Enrolment No: " + (String) dataSnapshot.child("enrolment_no").getValue());
                txt4.setText("Email: " + (String) dataSnapshot.child("email").getValue());
                txt5.setText("Department: " + (String) dataSnapshot.child("department").getValue());
                txt6.setText("Gender: " + (String) dataSnapshot.child("gender").getValue());
                Picasso.get().load(dataSnapshot.child("profileImage").getValue().toString()).into(circleImageView);
                loadingBar.dismiss();
                }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (role.equals("Admin")){
            Intent intent = new Intent(Profile.this, AdminDashboard.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Intent intent = new Intent(Profile.this, Dashboard.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
