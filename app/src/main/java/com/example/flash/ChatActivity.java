package com.example.flash;

import android.content.Context;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverId,messageReceiverName,getMessageReceiverImage,messageSenderId;

    private TextView userName,userLastSeen;
    private CircleImageView userImage;
    private ImageButton sendMessage;
    private EditText messageInput;
    private FirebaseAuth mAuth;

    private final List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;


    private DatabaseReference rootRef;

    private Toolbar chatActivityToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth=FirebaseAuth.getInstance();
        messageSenderId=mAuth.getCurrentUser().getUid();
        rootRef= FirebaseDatabase.getInstance().getReference();

        messageReceiverId=getIntent().getExtras().get("user_id").toString();
        messageReceiverName=getIntent().getExtras().get("user_name").toString();
        getMessageReceiverImage=getIntent().getExtras().get("user_image").toString();

        Log.i("flashhh",messageReceiverId);
        Log.i("flashhh",messageReceiverName);

        initializeControllers();


        userName.setText(messageReceiverName);
        Picasso.get().load(getMessageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);


        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToFirebase();
            }
        });



    }

    private void initializeControllers() {
        chatActivityToolbar=findViewById(R.id.chatToolbar);
        setSupportActionBar(chatActivityToolbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        userImage=findViewById(R.id.custom_profile_image);
        userName=findViewById(R.id.custom_profile_name);
        userLastSeen=findViewById(R.id.custom_profile_last_seen);
        sendMessage=findViewById(R.id.sendButton);
        messageInput=findViewById(R.id.messageInput);

        messageAdapter=new MessageAdapter(messagesList);
        userMessagesList=findViewById(R.id.messagesListOfUsers);
        linearLayoutManager=new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        rootRef.child("Messages").child(messageSenderId).child(messageReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();

                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendMessageToFirebase(){
        String messageText=messageInput.getText().toString();
        if(TextUtils.isEmpty(messageText)||TextUtils.isEmpty(messageText.trim())){

        }else{
            String messagesSenderRef="Messages/"+messageSenderId+"/"+messageReceiverId;
            String messagesReceiverRef="Messages/"+messageReceiverId+"/"+messageSenderId;

            DatabaseReference userMessageKeyRef=rootRef.child("Messages").child(messageSenderId)
                    .child(messageReceiverId).push();

            String messagePushId=userMessageKeyRef.getKey();

            Map messageTextbody=new HashMap();

            messageTextbody.put("message",messageText);
            messageTextbody.put("type","text");
            messageTextbody.put("from",messageSenderId);

            Map messageBodyDetails=new HashMap();

            messageBodyDetails.put(messagesSenderRef+"/"+messagePushId,messageTextbody);
            messageBodyDetails.put(messagesReceiverRef+"/"+messagePushId,messageTextbody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if(task.isSuccessful())
                        Log.i("flashhh","Message sent");
                    else{
                        Log.i("flashhh",task.getException().toString());
                    }
                    messageInput.setText("");
                }
            });
        }
    }
}