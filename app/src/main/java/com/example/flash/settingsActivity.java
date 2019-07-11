package com.example.flash;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextClock;
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

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class settingsActivity extends AppCompatActivity {


    private EditText userName,userStatus;
    private TextView updateAccountSettings;
    private CircleImageView userProfileImage;

    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initializeFields();

        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        rootRef= FirebaseDatabase.getInstance().getReference();

        retrieveUserInfo();

    }

    private void updateSettings() {
        String setUserName=userName.getText().toString();
        String setStatus=userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this,"please write your user name",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(setStatus)){
            Toast.makeText(this,"please write your status",Toast.LENGTH_SHORT).show();
        }else{
            HashMap<String,String> profileMap=new HashMap<>();
            profileMap.put("uid",currentUserId);
            profileMap.put("name",setUserName);
            profileMap.put("status",setStatus);

            rootRef.child("Users").child(currentUserId).setValue(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        sendUserToMainActivity();
                        Toast.makeText(getApplicationContext(),"Profile updated successfully",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(),"Error: "+task.getException(),Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }



    public void retrieveUserInfo(){
        rootRef.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists())&&(dataSnapshot.hasChild("name"))&&(dataSnapshot.hasChild("image"))){
                            String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus=dataSnapshot.child("status").getValue().toString();
                            String retrieveProfileImage=dataSnapshot.child("image").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);

                        }else if((dataSnapshot.exists())&&(dataSnapshot.hasChild("name"))){
                            String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus=dataSnapshot.child("status").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                        }else{
                            Toast.makeText(getApplicationContext(),"update your profile...",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void initializeFields(){
        updateAccountSettings=findViewById(R.id.updateSettingsButton);
        userName=findViewById(R.id.setUserName);
        userStatus=findViewById(R.id.setProfileStatus);
        userProfileImage=findViewById(R.id.setProfileImage);



    }

    private void sendUserToMainActivity() {
        Intent intent=new Intent(settingsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


}
