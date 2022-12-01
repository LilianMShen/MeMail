package com.example.memail;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DraftActivity extends AppCompatActivity {
    ImageButton saveButton;
    FirebaseFirestore db;
    CollectionReference saved;
    FirebaseAuth mAuth;
    ImageButton share;
    EditText draftContent;
    EditText draftTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        saveButton = findViewById(R.id.saveButton);
        db = FirebaseFirestore.getInstance();
        saved = db.collection("Saved");
        mAuth = FirebaseAuth.getInstance();

        Bundle extras = getIntent().getExtras();
        String docId = extras.getString("ID");

        draftContent = findViewById(R.id.draft);
        draftTitle = findViewById(R.id.draftTitle);

        DocumentReference docref = db.collection("Templates").document(docId);
        docref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Templates template = documentSnapshot.toObject(Templates.class);

                draftContent.setText(template.getDraft());
                draftTitle.setText(template.getTitle());
            }
        });

        share = findViewById(R.id.shareButton);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = draftContent.getText().toString();

                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_TEXT, message);
                email.putExtra(Intent.EXTRA_SUBJECT, draftTitle.getText().toString());

                //need this to prompts email client only
                email.setType("message/rfc822");

                startActivity(Intent.createChooser(email, "Choose an Email client :"));
            }
        });

        saveButton.setOnClickListener(this::onClick);
    }

    public void onClick(View v) {
        Toast.makeText(getApplicationContext(), "Email saved!", Toast.LENGTH_SHORT).show();

        Map<String, Object> data = new HashMap<>();
        EditText draft = findViewById(R.id.draft);
        EditText title = findViewById(R.id.draftTitle);
        data.put("Draft", draft.getText().toString());
        data.put("Title", title.getText().toString());
        data.put("UID", mAuth.getUid());
        saved.document().set(data);
    }
    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), FormatActivity.class);
        startActivity(myIntent);
        return true;
    }


}