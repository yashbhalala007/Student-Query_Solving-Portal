package com.example.adlproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminSearchItems extends AppCompatActivity {

    EditText editText;
    TextView textView;
    Button button;

    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_search_items);

        editText = findViewById(R.id.admin_search);
        button = findViewById(R.id.admin_search_btn);
        textView = findViewById(R.id.admin_search_content);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });
    }

    private void search() {
        String en_no = editText.getText().toString();

        userRef.orderByChild("email")
                .equalTo(en_no)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String key = dataSnapshot.child("uId").getValue().toString();
                        textView.setText(key);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
