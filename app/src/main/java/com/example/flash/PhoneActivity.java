package com.example.flash;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneActivity extends AppCompatActivity {
    private EditText phoneNumber;
    private EditText verifyCode;
    private Button sendVerificationCodeButton,verifyButton;

    private ProgressDialog loadingBar;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        phoneNumber=findViewById(R.id.phoneLayoutPhoneNumber);
        verifyCode=findViewById(R.id.phoneLayoutVerificationCode);
        sendVerificationCodeButton=findViewById(R.id.getVerificationCodeButton);
        verifyButton=findViewById(R.id.getVerifiedButton);
        loadingBar=new ProgressDialog(this);

        mAuth=FirebaseAuth.getInstance();


        sendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNo=phoneNumber.getText().toString();

                if(phoneNo.length()!=13)
                    Toast.makeText(PhoneActivity.this, "Enter a valid phone Number", Toast.LENGTH_SHORT).show();
                else{

                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("we are authenticating your phone");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNo,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks


                }
            }
        });


        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loadingBar.dismiss();
                String phoneNo=phoneNumber.getText().toString();
                if(phoneNo.charAt(0)!='+')
                Toast.makeText(getApplicationContext(),"Enter your country code properly",Toast.LENGTH_SHORT).show();
                else if(phoneNo.length()!=13){
                    Toast.makeText(getApplicationContext(),"Invalid number",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Something went wrong..",Toast.LENGTH_SHORT).show();
                }

            }

            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;
                loadingBar.dismiss();
                Toast.makeText(getApplicationContext(),"code sent..",Toast.LENGTH_SHORT).show();

            }
        };

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String verificationCode=verifyCode.getText().toString();

                if(verificationCode.length()!=6){
                    Toast.makeText(getApplicationContext(),"Enter the actual code sent",Toast.LENGTH_SHORT).show();
                }else{
                    loadingBar.setTitle("Code Verification");
                    loadingBar.setMessage("please wait, while we log you in..");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();



                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }

            }
        });

    }



    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(getApplicationContext(),"welcome",Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        } else {
                            String error=task.getException().toString();
                            Toast.makeText(getApplicationContext(),"Error "+error,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent intent=new Intent(PhoneActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
