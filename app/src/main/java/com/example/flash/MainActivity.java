package com.example.flash;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private TabsAccess mTabsAccess;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar=findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Flash");


        mViewPager=findViewById(R.id.main_tabs_pager);
        mTabsAccess=new TabsAccess(getSupportFragmentManager());
        mViewPager.setAdapter(mTabsAccess);

        mTabLayout=findViewById(R.id.min_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        rootRef=FirebaseDatabase.getInstance().getReference();


    }

    @Override
    protected void onStart() {

        super.onStart();
        if(currentUser==null){
            sendUserToLoginActivity();
        }else{
            verifyUserExistence();
        }
    }

    private void verifyUserExistence() {
        String currentUser=mAuth.getCurrentUser().getUid();
        rootRef.child("Users").child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("name").exists())){
                    //Toast.makeText(getApplicationContext(),"Welcome Back",Toast.LENGTH_SHORT).show();
                }
                else
                    sendUserToSettingsActivity(1);
                }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToLoginActivity() {
        Intent intent=new Intent(MainActivity.this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void sendUserToSettingsActivity() {
        Intent intent=new Intent(MainActivity.this,settingsActivity.class);
        startActivity(intent);
    }
    private void sendUserToSettingsActivity(int i) {
        Intent intent=new Intent(MainActivity.this,settingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void sendUserToFindFriendsActivity() {
        Intent intent=new Intent(MainActivity.this,FIndFriendsActivity.class);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()== R.id.main_logout_option){
            mAuth.signOut();
            sendUserToLoginActivity();
        }
        if(item.getItemId()== R.id.main_settings_option){
            sendUserToSettingsActivity();
        }
        if(item.getItemId()== R.id.main_find_friends){
            sendUserToFindFriendsActivity();
        }
        if(item.getItemId()== R.id.main_about_option){
            //send user to my website
            Intent intent=new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://gyaneswarsingh.000webhostapp.com/"));
            startActivity(intent);
        }
        if(item.getItemId()== R.id.main_group_option){
            requestNewGroup();
        }
        return true;
    }

    private void requestNewGroup() {
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group name");

        final EditText groupNameField=new EditText(MainActivity.this);
        groupNameField.setHint("Enter Group name here..");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName=groupNameField.getText().toString();
                if(TextUtils.isEmpty(groupName))
                    Toast.makeText(getApplicationContext(),"Enter proper group name",Toast.LENGTH_SHORT).show();
                else
                    groupCreator(groupName);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    private void groupCreator(String groupName) {
        rootRef.child("Groups").child(groupName).setValue("")
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                    Toast.makeText(getApplicationContext(),"Group created",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(),"Error"+task.getException(),Toast.LENGTH_LONG);
            }
        });
    }
}
