package com.example.flash;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class settingsActivity extends AppCompatActivity {


    private EditText userName,userStatus;
    private TextView updateAccountSettings;
    private CircleImageView userProfileImage;

    private String currentUserId,downloadUrl;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private static final int galleryPick=1;

    private StorageReference userProfileImageReference;
    private ProgressDialog loadingBar;


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
        userProfileImageReference= FirebaseStorage.getInstance().getReference().child("Profile Images");



        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,galleryPick);
            }
        });

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
                            Log.i("flashhh1",retrieveProfileImage);
                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);

                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);

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

        loadingBar=new ProgressDialog(this);



    }

    private void sendUserToMainActivity() {
        Intent intent=new Intent(settingsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==galleryPick && resultCode==RESULT_OK && data!=null){
            Uri imageUri=data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }


        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){

                loadingBar.setTitle("setting Profile Image");
                loadingBar.setMessage("please wait, uploading your profile image..");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri=result.getUri();


                final StorageReference filePath = userProfileImageReference.child(currentUserId + ".jpg");

                final UploadTask uploadTask=filePath.putFile(resultUri);
                
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String message = e.toString();
                        Toast.makeText(settingsActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(settingsActivity.this, "Image uploaded successfully...", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                        Task<Uri> urlTask=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if(!task.isSuccessful()){
                                    throw task.getException();

                                }

                                downloadUrl=filePath.getDownloadUrl().toString();
                                Log.i("flashhh",downloadUrl);
                                return filePath.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if(task.isSuccessful()){

                                    downloadUrl=task.getResult().toString();
                                    Log.i("flashhh2",downloadUrl);
                                    Toast.makeText(settingsActivity.this, "got image url", Toast.LENGTH_SHORT).show();
                                    saveImageUrlToDatabase();
                                    loadingBar.dismiss();

                                }
                            }
                        });
                    }
                });

            }
        }
    }

    private void saveImageUrlToDatabase() {
        rootRef.child("Users").child(currentUserId).child("image")
                .setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    loadingBar.dismiss();
                }else{
                    Toast.makeText(settingsActivity.this, "Error1"+task.getException(), Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }
}
