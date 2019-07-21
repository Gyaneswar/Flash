package com.example.flash;

import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiveUserId,currentState,senderUserId;

    private CircleImageView userProfileImage;
    private TextView userProfileName,userProfileStatus;
    private Button sendMessage,cancelRequest;

    private FirebaseAuth mAuth;

    private DatabaseReference usersRef,chatRequestRef ,contactsRef;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);



        receiveUserId=getIntent().getExtras().get("visit_user_id").toString();

        //Toast.makeText(this, "User ID : "+receiveUserId, Toast.LENGTH_SHORT).show();

        userProfileImage=findViewById(R.id.profileImage);
        userProfileName=findViewById(R.id.userName);
        userProfileStatus=findViewById(R.id.profileStatus);
        sendMessage=findViewById(R.id.sendMessageButton);
        cancelRequest=findViewById(R.id.cancelRequestButton);

        currentState="new";

        mAuth=FirebaseAuth.getInstance();
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef=FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        senderUserId=mAuth.getCurrentUser().getUid();
        retrieveUserInfo();

    }

    private void retrieveUserInfo() {
        usersRef.child(receiveUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() &&(dataSnapshot.hasChild("image"))){
                    String image=dataSnapshot.child("image").getValue().toString();
                    String name=dataSnapshot.child("name").getValue().toString();
                    String status=dataSnapshot.child("status").getValue().toString();


                    Picasso.get().load(image).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(name);
                    userProfileStatus.setText(status);

                    ManageChatRequest();


                }else{
                    String name=dataSnapshot.child("name").getValue().toString();
                    String status=dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(name);
                    userProfileStatus.setText(status);

                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequest() {

        chatRequestRef.child(senderUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(receiveUserId)){
                            String request_type=dataSnapshot.child(receiveUserId).child("request_type").getValue().toString();

                            if(request_type.equals("sent")){
                                currentState="sent";
                                sendMessage.setText("Cancel Chat request");
                                sendMessage.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                                sendMessage.setAllCaps(false);

                            }else if(request_type.equals("received")){
                                currentState="received";
                                sendMessage.setText("Accept Chat Request");
                                sendMessage.setEnabled(true);
                                cancelRequest.setVisibility(View.VISIBLE);
                                sendMessage.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                                sendMessage.setAllCaps(false);

                                cancelRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        cancelChatRequest();
                                    }
                                });
                            }
                        }
                        else{
                            contactsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(receiveUserId)){
                                                currentState="friends";
                                                sendMessage.setText("Remove This Contact");
                                                sendMessage.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                                                sendMessage.setAllCaps(false);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        if(!senderUserId.equals(receiveUserId)){
            sendMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage.setEnabled(false);

                    if(currentState.equals("new")){
                        sendChatRequest();
                    }
                    if(currentState.equals("sent")){
                        cancelChatRequest();
                    }
                    if(currentState.equals("received")){
                        acceptChatRequest();
                    }
                    if(currentState.equals("friends")){
                        removeContact();
                    }
                }
            });

        }else{
            sendMessage.setVisibility(View.INVISIBLE);
        }

    }

    private void removeContact() {
        contactsRef.child(senderUserId).child(receiveUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            contactsRef.child(receiveUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendMessage.setEnabled(true);
                                                currentState="new";
                                                sendMessage.setText("Send Message");
                                                sendMessage.setAllCaps(false);
                                                sendMessage.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                                                Toast.makeText(ProfileActivity.this, "Request Cancelled", Toast.LENGTH_SHORT).show();

                                                cancelRequest.setVisibility(View.INVISIBLE);
                                                cancelRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void acceptChatRequest() {
        contactsRef.child(senderUserId).child(receiveUserId)
                .child("Contacts").setValue("saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            contactsRef.child(receiveUserId).child(senderUserId)
                                    .child("Contacts").setValue("saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                chatRequestRef.child(senderUserId).child(receiveUserId)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            chatRequestRef.child(senderUserId).child(receiveUserId)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    sendMessage.setEnabled(true);
                                                                    cancelRequest.setEnabled(false);
                                                                    cancelRequest.setVisibility(View.INVISIBLE);
                                                                    sendMessage.setText("Remove Contact");
                                                                    sendMessage.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                                                                    sendMessage.setAllCaps(false);
                                                                    Toast.makeText(ProfileActivity.this, "Added to Contacts", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                        }
                    }
                });
        
    }

    private void cancelChatRequest() {
        chatRequestRef.child(senderUserId).child(receiveUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                             if(task.isSuccessful()){
                                 chatRequestRef.child(receiveUserId).child(senderUserId)
                                         .removeValue()
                                         .addOnCompleteListener(new OnCompleteListener<Void>() {
                                             @Override
                                             public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        sendMessage.setEnabled(true);
                                                        currentState="new";
                                                        sendMessage.setText("Send Message");
                                                        sendMessage.setAllCaps(false);
                                                        sendMessage.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                                                        Toast.makeText(ProfileActivity.this, "Request Cancelled", Toast.LENGTH_SHORT).show();

                                                        cancelRequest.setVisibility(View.INVISIBLE);
                                                        cancelRequest.setEnabled(false);
                                                    }
                                             }
                                         });
                             }
                    }
                });
    }

    private void sendChatRequest() {
        chatRequestRef.child(senderUserId).child(receiveUserId)
        .child("request_type").setValue("sent")
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    chatRequestRef.child(receiveUserId).child(senderUserId)
                            .child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        sendMessage.setEnabled(true);
                                        currentState="sent";
                                        sendMessage.setText("Cancel Chat request");
                                        sendMessage.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                                    }
                                }
                            });

                    Toast.makeText(ProfileActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
