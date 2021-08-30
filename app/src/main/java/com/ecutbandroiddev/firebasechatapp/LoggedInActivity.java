package com.ecutbandroiddev.firebasechatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LoggedInActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private FirebaseAuth auth;
    DatabaseReference reference;

    Button logOut, sendMsg;
    EditText messageInput;
    ListView chatMessages;
    ArrayAdapter arrayAdapter;
    ArrayList<String> messages = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        logOut = findViewById(R.id.logOutButton);
        chatMessages = findViewById(R.id.messageList);
        messageInput = findViewById(R.id.messageInput);
        sendMsg = findViewById(R.id.sendButton);

        auth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        logOut.setOnClickListener(new View.OnClickListener() { // Logs user out and returns them to the login screen
            @Override
            public void onClick(View v) {
                auth.signOut();
                Intent intent = new Intent(LoggedInActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        sendMsg.setOnClickListener(new View.OnClickListener() { // Updates the database with the message according to the message model
            @Override
            public void onClick(View v) {
                if (!messageInput.getText().toString().isEmpty()) {

                    reference.push().setValue(new MessageModel(messageInput.getText().toString(), auth.getCurrentUser().getEmail()))
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    messageInput.setText("");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoggedInActivity.this, "Error. Message was not sent", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });

        reference.addValueEventListener(new ValueEventListener() { // When the database is updated with a new message, update the list view with it
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {

                    MessageModel result = ds.getValue(MessageModel.class);
                    String messageText = result.getMessageText();
                    String messageUser = result.getMessageUser();
                    String messageTime = new SimpleDateFormat("MM/dd/yyyy (HH:mm:ss)").format(new Date(result.getMessageTime()));

                    messages.add(messageTime + " " + messageUser + ": " + messageText);

                }

                arrayAdapter = new ArrayAdapter(LoggedInActivity.this, android.R.layout.simple_list_item_1, messages);
                chatMessages.setAdapter(arrayAdapter);
                chatMessages.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                chatMessages.setStackFromBottom(true);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

    }
}