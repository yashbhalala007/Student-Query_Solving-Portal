package com.example.adlproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddPhotos extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton ib;
    private EditText ed;
    private Button btn;

    final static int Gallery_Pick = 1;
    private Uri ImageUri;
    private String description;
    private ProgressDialog loadingBar;

    private StorageReference postReference;
    private DatabaseReference usersRef, postRef, backupRef;
    private FirebaseAuth mAuth;

    private String saveDate, saveTime, postName, current_user_id;
    private String  downloadUrl;
    private long countPhotos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photos);

        ib = findViewById(R.id.image_button);
        ed = findViewById(R.id.ed_about_post);
        btn = findViewById(R.id.btn_add_post);

        mToolbar = findViewById(R.id.add_photos_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Add Photo");

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
        postReference = FirebaseStorage.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        backupRef = FirebaseDatabase.getInstance().getReference().child("Backup").child("Posts");

        loadingBar = new ProgressDialog(this);



        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if(id == android.R.id.home){
            Intent intent = new Intent(AddPhotos.this, Photos.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void ValidatePostInfo() {
        description = ed.getText().toString();
        if (ImageUri == null){
            Toast.makeText(AddPhotos.this,"Please Select Image First!!", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(description)){
            Toast.makeText(AddPhotos.this,"Please Write Description First!!", Toast.LENGTH_LONG).show();
        }
        else {
            loadingBar.setTitle("Post");
            loadingBar.setMessage("Post is adding");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            StoringImage();
        }
    }

    private void StoringImage() {
        Calendar calDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveDate = currentDate.format(calDate.getTime());

        Calendar calTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveTime = currentTime.format(calTime.getTime());

        postName = saveDate + saveTime;
        final StorageReference filePath = postReference.child("Post Images").child(ImageUri.getLastPathSegment() + postName + ".jpg");
        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){

                    filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            downloadUrl = task.getResult().toString();
                            SavingPostInfo();
                        }
                    });
                }
                else {
                    String msg = task.getException().getMessage();
                    Toast.makeText(AddPhotos.this,"Error Occured: " + msg, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void SavingPostInfo() {

        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    countPhotos = dataSnapshot.getChildrenCount();
                }
                else {
                    countPhotos = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    String userFullName = dataSnapshot.child("name").getValue().toString();
                    String en_no = dataSnapshot.child("enrolment_no").getValue().toString();

                    HashMap postMap = new HashMap();
                    postMap.put("uid", current_user_id);
                    postMap.put("date", saveDate);
                    postMap.put("time", saveTime);
                    postMap.put("description", description);
                    postMap.put("postImage", downloadUrl);
                    postMap.put("name", userFullName);
                    postMap.put("counter", countPhotos);
                    postMap.put("profileImage", dataSnapshot.child("profileImage").getValue().toString());


                    postRef.child(current_user_id + postName).updateChildren(postMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                }
                            });

                    postMap.put("enrolmentNo", en_no);
                    postMap.put("status", "Active");
                    backupRef.child(en_no).child(current_user_id + postName).updateChildren(postMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()){

                                        Intent intent = new Intent(AddPhotos.this, Photos.class);
                                        startActivity(intent);
                                        Toast.makeText(AddPhotos.this,"Post added Successfully!!!", Toast.LENGTH_LONG).show();
                                        loadingBar.dismiss();
                                    }
                                    else {
                                        String msg = task.getException().getMessage();
                                        Toast.makeText(AddPhotos.this,"Error Occured: " + msg, Toast.LENGTH_LONG).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            ImageUri = data.getData();
            ib.setImageURI(ImageUri);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AddPhotos.this, Photos.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
