package com.lhester.esarakan;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static java.lang.Thread.sleep;

public class logincustomer extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String emm = "";
    private String pas = "";
    private ProgressDialog loadingbar;
    Button btnlogin, btncancel;
    private SweetAlertDialog pDialog = null;

    private CheckBox saveLoginCheckBox;
    public static final String PREFS_NAME = "MyPrefsFile";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";
    private EditText em, ps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logincustomer);
        mAuth = FirebaseAuth.getInstance();
        btnlogin = findViewById(R.id.btnlogin);
        em = findViewById(R.id.txtemail);
        ps = findViewById(R.id.txtpassword);

        saveLoginCheckBox = findViewById(R.id.checkBox);

        SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String username = pref.getString(PREF_USERNAME, null);
        String password = pref.getString(PREF_PASSWORD, null);
        if (username != null && password != null) {
            saveLoginCheckBox.setChecked(false);
            em.setText(username);
            ps.setText(password);
        }

        loadingbar = new ProgressDialog(logincustomer.this);
        chkLocation();
        chkCourserLoc();
        checkPermission();
        checkPermissionVibrate();

        // if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // only for gingerbread and newer versions
        //      btncancel.setText("Android 10");
        //   }
        //   else {
        //        btncancel.setText("Not Android 10");
        //  }
//        btncancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //System.exit(0);
//                // finish();
//                //  startActivity(new Intent(logincustomer.this, SplashScreen.class));
//                finish();
//
//            }
//        });
        btnlogin.setOnClickListener(v -> {

            emm = em.getText().toString();
            pas = ps.getText().toString();
            if (emm.equals("")) {
                Toast.makeText(logincustomer.this, "Check Email ", Toast.LENGTH_SHORT).show();
            }
            if (pas.equals("")) {
                Toast.makeText(logincustomer.this, "Check Password ", Toast.LENGTH_SHORT).show();
            }
            if (!pas.equals("") && !emm.equals("")) {
                pDialog = new SweetAlertDialog(logincustomer.this, SweetAlertDialog.PROGRESS_TYPE);
                pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                pDialog.setTitleText("Lessor Login");
                pDialog.setContentText("Please Wait..");
                pDialog.setCancelable(false);
                pDialog.show();
                mAuth.signInWithEmailAndPassword(emm, pas)
                        .addOnCompleteListener(logincustomer.this, task -> {
                            if (!task.isSuccessful()) {


                                Toast.makeText(logincustomer.this, "Login Error: " + task.getException(), Toast.LENGTH_SHORT).show();

                                // loadingbar.dismiss();
                                Log.e("errors", String.valueOf(task.getException()));
                                pDialog.dismiss();
                            } else {
                                getUserInformation();
                                Log.e("customerVerify", String.valueOf(customerVerify));


                            }
                        });
            }
        });
//        btncancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                System.exit(0);
//                return;
//            }
//        });
        isGPSenabled();
        isNetworkAvailable();
        validate_();
    }

    private void chkLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 3);
        }
    }

    private void chkCourserLoc() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 4);
        }
    }

    public void Register_Customer(View v) {
        Intent intent = new Intent(logincustomer.this, TryPhone.class);
        startActivity(intent);
        finish();
        return;
    }

    public void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
        }

    }

    public void checkPermissionVibrate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.VIBRATE}, 2);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.READ_PHONE_STATE)) {
                    // check whether storage permission granted or not.
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // do what you want;

                    } else {

                    }
                }
            case 3:
                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // check whether storage permission granted or not.
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    } else {
                        finish();
                    }
                }
            case 4:
                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    // check whether storage permission granted or not.
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    } else {
                        finish();
                    }
                }
            case 2:
                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.VIBRATE)) {
                    // check whether storage permission granted or not.
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // do what you want;

                    } else {
                        finish();
                    }
                }
                break;
            default:
                break;
        }
    }

    private DatabaseReference databaseReference;
    private String customerVerify = "false";

    private void getUserInformation() {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers");
        databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 2) {
                    customerVerify = "true";
                    //   btnlogin.setText(customerVerify);
                    if (customerVerify.equals("true")) {
                        if (dataSnapshot.child("status").getValue().toString().equals("Active")) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(em.getWindowToken(), 0);

                            if (saveLoginCheckBox.isChecked()) {
                                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                        .edit()
                                        .putString(PREF_USERNAME, em.getText().toString())
                                        .putString(PREF_PASSWORD, ps.getText().toString())
                                        .apply();
                            }
                            if (checkIfEmailVerified()) {
                                Intent intent = new Intent(logincustomer.this, MainActivity.class);
                                startActivity(intent);
                                //  Toast.makeText(logincustomer.this, "Log in Successful", Toast.LENGTH_SHORT).show();
                                //loadingbar.dismiss();

                                pDialog.dismiss();
                                finish();
                            } else {
                                pDialog.dismiss();
                                sendVerificationEmail();

                            }

                        } else {

                            new SweetAlertDialog(logincustomer.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Error Login")
                                    .setContentText("Your Account is suspended,please contact system administrator")
                                    .setConfirmText("Ok")
                                    .setConfirmClickListener(sDialog -> {
                                        sDialog.dismissWithAnimation();
                                        pDialog.dismiss();
                                    })
                                    .show();
                        }
                        //   Toast.makeText(logincustomer.this, "Your Account is suspended,please contact administrator", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    Toast.makeText(logincustomer.this, "Login Error", Toast.LENGTH_SHORT).show();
                    pDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private boolean checkIfEmailVerified() {
        boolean verified;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user.isEmailVerified()) {
            verified = true;
            //  Toast.makeText(LogInDriver.this, "Verified", Toast.LENGTH_LONG).show();
        } else {
            verified = false;
            //  Toast.makeText(LogInDriver.this, "Not Verified", Toast.LENGTH_LONG).show();
            // sendVerificationEmail();
            // FirebaseAuth.getInstance().signOut();
            //  tartActivity(new Intent(LogInDriver.this, MainActivity.class));
            //  finish();
        }
        return verified;

    }

    private void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            new SweetAlertDialog(logincustomer.this, SweetAlertDialog.WARNING_TYPE)
                                    .setTitleText("Email Verification")
                                    .setContentText("Email is not yet Verified and please check your email for the link verification ")
                                    .setConfirmText("  OK  ")
                                    .setConfirmClickListener(sDialog -> sDialog.dismissWithAnimation())
                                    .show();
                            // Toast.makeText(LogInDriver.this, "Send", Toast.LENGTH_LONG).show();
                        } else {
                            //   Toast.makeText(LogInDriver.this, "Error Send", Toast.LENGTH_LONG).show();
                            new SweetAlertDialog(logincustomer.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Email Verification")
                                    .setContentText("Email is not yet Verified and an error on sending email verification")
                                    .setConfirmText("  OK  ")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sDialog) {
                                            sDialog.dismissWithAnimation();
                                        }
                                    })
                                    .show();
                        }
                    });
        }
    }

    private void validate_() {
        //    if(isNet && isGps)
        if (isGps) {
            // btnNext.setEnabled(true);

        } else {
            //showLocationDisabledInfo();
            onGPS2();
            //  btnNext.setEnabled(false);
        }
    }

    private Boolean isNet = false;

    private void isNetworkAvailable() {

        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (manager != null) {
            networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null) {
                isNet = true;
            }
        }


    }

    private Boolean isGps = false;

    private void isGPSenabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean providerEnables = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (providerEnables) {
            isGps = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MyDialog != null) {
            MyDialog.dismiss();
        }
        chkLocation();
        chkCourserLoc();
        checkPermission();
        checkPermissionVibrate();

        isGPSenabled();
        isNetworkAvailable();
        validate_();
        // Log.e("Switch State=","resumed");
    }

    SweetAlertDialog MyDialog;

    private void onGPS2() {
        MyDialog = new SweetAlertDialog(logincustomer.this, SweetAlertDialog.WARNING_TYPE);
        MyDialog.setTitleText("Location");
        MyDialog.setContentText("Please Enable GPS / Location");
        MyDialog.setConfirmText("YES");
        MyDialog.setConfirmClickListener(sDialog -> {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            sDialog.dismiss();
        });
        MyDialog.setCancelButton("NO", sDialog -> {
            sDialog.dismissWithAnimation();
            finish();

        });
        MyDialog.show();
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        //    super.onBackPressed();
        new SweetAlertDialog(logincustomer.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("App Exit")
                .setContentText("You sure you want to close the app?")
                .setConfirmText("Exit")
                .setConfirmClickListener(sDialog -> {
                    System.exit(0);
                    sDialog.dismissWithAnimation();
                })
                .setCancelButton("Cancel", sDialog -> sDialog.dismissWithAnimation())
                .show();
    }
}
