package com.example.adlproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class AnswerActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton ib1;
    private EditText ed2;
    private Button btn;
    private RecyclerView recyclerView;

    final static int Gallery_Pick = 1;
    private ArrayList<Uri> ImageUri = new ArrayList<Uri>();
    private String description;
    private ImageView[] imageView = new ImageView[1];

    private StorageReference questionReference;
    private DatabaseReference usersRef, questionRef, backupRef;
    private FirebaseAuth mAuth;

    private String current_user_id,saveDate, saveTime, postName, department, postKey, en_no;
    private int noOfImages = -1, j = 0, counter = 1;
    private long countQuestions = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);

        ib1 = findViewById(R.id.add_ans_add_image_btn);
        ed2 = findViewById(R.id.add_ans_des);
        btn = findViewById(R.id.add_ans_send);

        recyclerView = findViewById(R.id.add_ans_images);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        department = getIntent().getExtras().get("Depart").toString();
        postKey = getIntent().getExtras().get("PostKey").toString();
        en_no = getIntent().getExtras().get("en_no").toString();

        mToolbar = findViewById(R.id.add_ans_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Add Answer");

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
        questionReference = FirebaseStorage.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        questionRef = FirebaseDatabase.getInstance().getReference().child("Questions");
        backupRef = FirebaseDatabase.getInstance().getReference().child("Backup").child("Questions");

        ib1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = "";
                if (ImageUri.size() > 0) {
                    for (int i = 0; i < ImageUri.size(); i++){
                        s = s + ImageUri.get(i).toString() + "\n";
                    }
                }
                Toast.makeText(AnswerActivity.this, s,Toast.LENGTH_LONG).show();
                validatePostInfo();
            }
        });

    }

    private void validatePostInfo() {
        description = ed2.getText().toString();
        if (TextUtils.isEmpty(description)){
            Toast.makeText(AnswerActivity.this, "Please Write Answer First!!",Toast.LENGTH_LONG).show();
        }else {
            StoringImage();
        }
    }

    private void StoringImage() {
        Calendar calDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveDate = currentDate.format(calDate.getTime());

        Calendar calTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveTime = currentTime.format(calTime.getTime());

        postName = current_user_id + saveDate + saveTime;

        savingPostInfo();

        if (ImageUri.size() > 0){
            StorageReference filePath = questionReference.child("Answer Images").child(postName);
            for (j = 0; j < ImageUri.size(); j++){
                Uri individualImage = ImageUri.get(j);
                final StorageReference reference = filePath.child(individualImage.getLastPathSegment() + postName + ".jpg");
                reference.putFile(individualImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Log.i("Download", "Success 1");
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String temp = String.valueOf(uri);
                                storeImageData(temp, j);
                                //downloadUrl.add(temp);
                                //Log.i("Download", temp);
                                //Log.i("Download", "Success 2");
                            }
                        });
                    }
                });

            }
        }
        backToHome();
    }

    private void backToHome() {
        Intent intent = new Intent(AnswerActivity.this, FullQuestionActivity.class);
        intent.putExtra("PostKey", postKey);
        intent.putExtra("dept", department);
        startActivity(intent);
        Toast.makeText(AnswerActivity.this, "Answer Added Successfully",Toast.LENGTH_LONG).show();
    }

    private void storeImageData(final String temp, final int j) {
        usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String department = dataSnapshot.child("department").getValue().toString();

                    String nameOfPost = "Image" + counter++;
                    DatabaseReference databaseReference = questionRef.child(department).child(postKey).child("Answers").child(postName).child("Images").child(nameOfPost).child("Image");
                    databaseReference.setValue(temp);

                    DatabaseReference reference = backupRef.child(en_no).child(postKey).child("Answers").child(postName).child("Images").child(nameOfPost).child("Image");
                    reference.setValue(temp);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void savingPostInfo() {

        questionRef.child(department).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    countQuestions = dataSnapshot.getChildrenCount();
                } else {
                    countQuestions = 0;
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
                    String e_no = dataSnapshot.child("enrolment_no").getValue().toString();

                    HashMap postMap = new HashMap();
                    postMap.put("uid", current_user_id);
                    postMap.put("date", saveDate);
                    postMap.put("time", saveTime);
                    postMap.put("answer", description);
                    postMap.put("name", userFullName);
                    postMap.put("enrolment_no", e_no);
                    postMap.put("counter", countQuestions);

                    questionRef.child(department).child(postKey).child("Answers").child(postName).updateChildren(postMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        //Log.i("Download", "PostData");
                                    }
                                }
                            });

                    backupRef.child(en_no).child(postKey).child("Answers").child(postName).updateChildren(postMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        //Completed
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

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){

            noOfImages = noOfImages + 1;
            if (noOfImages > 0) {
                ImageView imageView1[] = new ImageView[noOfImages + 1];
                {
                    for (int i = 0; i < noOfImages; i++){
                        imageView1[i] = imageView[i];
                    }
                }
                imageView = imageView1;
            }
            ImageUri.add(data.getData());
            newImageAdd(noOfImages);
        }
    }

    private void newImageAdd(int noOfImages) {
        ImagesAdapter imagesAdapter = new ImagesAdapter(this, ImageUri);
        recyclerView.setAdapter(imagesAdapter);
    }

    private class ImagesAdapter extends RecyclerView.Adapter<AskQuestion.imagesViewHolder>{

        private Context c;
        private ArrayList<Uri> uris;

        public ImagesAdapter(Context c, ArrayList<Uri> uris) {
            this.c = c;
            this.uris = uris;
        }

        @NonNull
        @Override
        public AskQuestion.imagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(c).inflate(R.layout.add_question_images_layout, parent, false);
            return new AskQuestion.imagesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final AskQuestion.imagesViewHolder holder, final int position) {
            holder.imageView.setImageURI(uris.get(position));
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageUri.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, ImageUri.size());
                    Toast.makeText(AnswerActivity.this, "Position is " + position, Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return uris.size();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AnswerActivity.this, Dashboard.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
