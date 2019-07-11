package com.example.flash;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private EditText userEmail,userPassword,phoneNumber,confirmPassword;
    private TextView alreadyHaveAnAccount;
    private Button register;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userEmail=findViewById(R.id.registerEmail);
        userPassword=findViewById(R.id.registerPassword);
        confirmPassword=findViewById(R.id.registerConfirmPassword);
        phoneNumber=findViewById(R.id.phoneNumber);
        alreadyHaveAnAccount=findViewById(R.id.alreadyHaveAnAccount);
        register=findViewById(R.id.register2);
        loadingBar=new ProgressDialog(this);
        rootRef= FirebaseDatabase.getInstance().getReference();

        //firebase object initialization
        mAuth=FirebaseAuth.getInstance();



        //send the user to login activity if the user already have a account
        alreadyHaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginActivity();
            }
        });


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkValidityOfCredentials())
                    createNewAccount();
                else{
                    userEmail.setText("");
                    userPassword.setText("");
                    confirmPassword.setText("");
                    phoneNumber.setText("");
                }
            }
        });
    }
    //create new account
    private void createNewAccount() {
        String email=userEmail.getText().toString();
        String password=userPassword.getText().toString();
        loadingBar.setTitle("Logging in..");
        loadingBar.setMessage("please wait..");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();
        mAuth.createUserWithEmailAndPassword(email,password)
        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    String currentUserId=mAuth.getCurrentUser().getUid();
                    rootRef.child("Users").child(currentUserId).setValue("");


                    Toast.makeText(getApplicationContext(),"Account Created",Toast.LENGTH_SHORT).show();
                    sendUserToMainActivity();
                    loadingBar.dismiss();
                }else{
                    Toast.makeText(getApplicationContext(),"Error"+task.getException().toString(),Toast.LENGTH_LONG).show();
                    loadingBar.dismiss();
                }
            }
        });
    }

    //check weather the credentials entered by the user are correct or not
    private boolean checkValidityOfCredentials() {
        if(!(userEmail.getText().toString().contains("@")&&userEmail.getText().length()>0)){
            Toast.makeText(getApplicationContext(),"Invalid Email",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!userPassword.getText().toString().equals(confirmPassword.getText().toString())){
            Toast.makeText(getApplicationContext(),"Passwords don't match",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(phoneNumber.length()!=10){
            Toast.makeText(getApplicationContext(),"Invalid Phone Number",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //send user to login activity
    private void sendUserToLoginActivity() {
        Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    private void sendUserToMainActivity() {
        Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}
