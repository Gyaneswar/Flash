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
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {


private FirebaseUser currentUser;
private FirebaseAuth mAuth;

private ProgressDialog loadingBar;

private Button loginButton;
private EditText userEmail,userPassword;
private TextView needNewAccountLink,forgetPasswordLink,phoneLoginButton;


//    @Override
//    protected void onStart() {
//
//        super.onStart();
//        if(currentUser!=null){
//            sendUserToMainActivity();
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        initializeFields();
        needNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToRegisterActivity();
            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkValidityOfCredentials());
                    allowUsersToLogin();
            }
        });

        phoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginActivity();
            }
        });

    }

    private void sendUserToLoginActivity() {
        Intent intent=new Intent(LoginActivity.this,PhoneActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void allowUsersToLogin() {
        String email=userEmail.getText().toString();
        String password=userPassword.getText().toString();
        loadingBar.setTitle("Creating Account");
        loadingBar.setMessage("please wait..");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();
        mAuth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getApplicationContext(), "Logging you in..", Toast.LENGTH_SHORT).show();
                        sendUserToMainActivity();
                        loadingBar.dismiss();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Error" + task.getException().toString(), Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }
                }
            });
    }


    private void initializeFields() {
        loginButton=findViewById(R.id.login);
        phoneLoginButton=findViewById(R.id.loginWithPhone);
        userEmail=findViewById(R.id.loginEmail);
        userPassword=findViewById(R.id.loginPassword);
        needNewAccountLink=findViewById(R.id.register1);
        forgetPasswordLink=findViewById(R.id.forgotPassword);
        loadingBar=new ProgressDialog(this);

    }



    private void sendUserToMainActivity() {
        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void sendUserToRegisterActivity() {
        Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private boolean checkValidityOfCredentials(){
        if(!(userEmail.getText().toString().contains("@")&&userEmail.getText().length()>0)) {
            Toast.makeText(getApplicationContext(), "Invalid Email", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


}
