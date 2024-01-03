package com.lhester.esarakan;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class TryPhone extends AppCompatActivity {
    private Button btnLogin;
    private EditText etPhoneNumber;
    private static final String TAG = "Try";
    private TextView txtmsg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_try_phone);
        btnLogin = findViewById(R.id.btnLogin);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        txtmsg=findViewById(R.id.txtmessage);
        txtmsg.setText("");

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String phoneNumber = etPhoneNumber.getText().toString();
                if (phoneNumber.isEmpty())
                    Toast.makeText(TryPhone.this, "Enter your phone number", Toast.LENGTH_SHORT).show();
                else {
                    //verify phone number
                    txtmsg.setText("Please wait do not close.. sending verification code");
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            "+63"+phoneNumber, 60, TimeUnit.SECONDS, TryPhone.this,
                            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                @Override
                                public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                                    //  signInUser(phoneAuthCredential);
                                    Log.d(TAG, "Verified:"+phoneAuthCredential);
                                }

                                @Override
                                public void onVerificationFailed(FirebaseException e) {
                                    Log.d(TAG, "onVerificationFailed:"+e.getLocalizedMessage());
                                }

                                @Override
                                public void onCodeSent(final String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                    super.onCodeSent(verificationId, forceResendingToken);
                                    //
                                    Dialog dialog = new Dialog(TryPhone.this);
                                    dialog.setContentView(R.layout.verify_popup);
                                    dialog.setCancelable(false);

                                    final EditText etVerifyCode = dialog.findViewById(R.id.etVerifyCode);
                                    Button btnVerifyCode = dialog.findViewById(R.id.btnVerifyOTP);
                                    btnVerifyCode.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            String verificationCode = etVerifyCode.getText().toString();
                                            if(verificationId.isEmpty()) return;
                                            //create a credential
                                            // PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationId,verificationCode);
                                            //  signInUser(credential);
                                            Common_Variables.pn ="+63"+phoneNumber;
                                            Intent intent=new Intent(TryPhone.this,register_customer.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });

                                    dialog.show();
                                }
                            });
                }
            }
        });
    }
}