package com.example.flash;

import android.graphics.Color;
import android.os.FileObserver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton mImageButton;
    private EditText mEditText;
    private ScrollView mScrollView;
    private TextView mTextView;
    private LinearLayout mLinearLayout;

    private String groupName,currentUserId,currentUserName,currentDate,currentTime,messageKey;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef,groupNameRef,groupMessageKeyRef;

    @Override
    protected void onStart() {
        super.onStart();

        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    displayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    displayMessages(dataSnapshot);
                }
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

    private void displayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator=dataSnapshot.getChildren().iterator();

        while(iterator.hasNext()){
            String chatDate=(String)((DataSnapshot)iterator.next()).getValue();
            String chatMessage=(String)((DataSnapshot)iterator.next()).getValue();
            String chatName=(String)((DataSnapshot)iterator.next()).getValue();
            String chatTime=(String)((DataSnapshot)iterator.next()).getValue();

            mTextView.append(chatName+" :\n"+chatMessage+" \n"+chatTime+"   "+chatDate+"\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);




        //mTextView.setText(groupName);

        groupName=getIntent().getExtras().get("groupName").toString();
        Toast.makeText(getApplicationContext(),groupName,Toast.LENGTH_SHORT).show();
        initializeFields();
        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        rootRef= FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(groupName);
        mLinearLayout=findViewById(R.id.chatLinearLayout);

        getUserInfo();

        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToDatabase();
                mEditText.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }

    private void sendMessageToDatabase() {
        String message=mEditText.getText().toString();
        messageKey=groupNameRef.push().getKey();
        if(TextUtils.isEmpty(message)){

        }else{
            Calendar calForDate=Calendar.getInstance();
            SimpleDateFormat currentDateFormat=new SimpleDateFormat("mmm dd, yyyy");
            currentDate=currentDateFormat.format(calForDate.getTime());


            Calendar calForTime=Calendar.getInstance();
            SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm a");
            currentTime=currentTimeFormat.format(calForTime.getTime());


            HashMap<String,Object> groupMessageKey=new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);

            groupMessageKeyRef=groupNameRef.child(messageKey);

            HashMap<String,Object> messageInfoMap=new HashMap<>();
            messageInfoMap.put("name",currentUserName);
            messageInfoMap.put("message",message);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("time",currentTime);
            groupMessageKeyRef.updateChildren(messageInfoMap);



        }

    }

    private void getUserInfo() {
        rootRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              if(dataSnapshot.exists()){
                  currentUserName=dataSnapshot.child("name").getValue().toString();
              }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeFields() {
        mToolbar=findViewById(R.id.groupChatBarLayout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(groupName);

        mImageButton=findViewById(R.id.sendButton);
        mEditText=findViewById(R.id.messageInput);
        mTextView=findViewById(R.id.groupChatTextDisplay);
        mScrollView=findViewById(R.id.scrollView);
    }


}
