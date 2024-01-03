package com.lhester.esarakan;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class register_customer extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference CustomerDatabaseRef;

    private String em = "";
    private String pas = "", faddress = "";
    private EditText lname, fname, address, contact, txtpass2;
    private Spinner municipality, gen;
    private String CustomerOnlineID;
    private ProgressDialog loadingbar;

    private CheckBox checkBox;
    private Button btnreg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_customer);
        mAuth = FirebaseAuth.getInstance();

        checkBox = findViewById(R.id.terms);
        lname = findViewById(R.id.lname);
        fname = findViewById(R.id.fname);
        address = findViewById(R.id.address);
        municipality = findViewById(R.id.municipality);
        gen = findViewById(R.id.gender);
        contact = findViewById(R.id.contact);
        contact.setText(Common_Variables.pn);
        txtpass2 = findViewById(R.id.txtpassword2);
        ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter
                .createFromResource(register_customer.this, R.array.municipality,
                        R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        staticAdapter.setDropDownViewResource(R.layout.spinner_drop_down);
        // Apply the adapter to the spinner
        municipality.setAdapter(staticAdapter);


        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check_terms();
            }
        });
        ArrayAdapter<CharSequence> staticAdapter2 = ArrayAdapter
                .createFromResource(register_customer.this, R.array.gen,
                        R.layout.spinner_gender);
        // Specify the layout to use when the list of choices appears
        staticAdapter2.setDropDownViewResource(R.layout.spinner_drop_down);
        // Apply the adapter to the spinner
        gen.setAdapter(staticAdapter2);


        btnreg = findViewById(R.id.btnregister);
        Button btncancel = findViewById(R.id.btncancel);
        final EditText txtemail = findViewById(R.id.txtemail);
        final EditText txtpas = findViewById(R.id.txtpassword);
        loadingbar = new ProgressDialog(this);
        btnreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                em = txtemail.getText().toString();
                pas = txtpas.getText().toString();
                if (pas.length() >= 6) {
                    if (txtpass2.getText().toString().equals(pas)) {
                        if (em == "") {
                            Toast.makeText(register_customer.this, "Check Email ", Toast.LENGTH_SHORT).show();
                        }
                        if (pas == "") {
                            Toast.makeText(register_customer.this, "Check Password ", Toast.LENGTH_SHORT).show();
                        } else {
                            loadingbar.setTitle("Customer Registration");
                            loadingbar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            loadingbar.setIndeterminate(true);
                            loadingbar.setCancelable(false);
                            loadingbar.setMessage("Please Wait..");
                            loadingbar.show();
                            mAuth.createUserWithEmailAndPassword(em, pas)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                CustomerOnlineID = mAuth.getCurrentUser().getUid();
                                                CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference()
                                                        .child("Users").child("Customers").child(CustomerOnlineID);
                                                CustomerDatabaseRef.setValue(true);
                                                faddress = address.getText().toString() + ", " + municipality.getSelectedItem().toString();
                                                HashMap<String, Object> userMap = new HashMap<>();
                                                userMap.put("first_name", fname.getText().toString());
                                                userMap.put("last_name", lname.getText().toString());
                                                userMap.put("phone", contact.getText().toString());
                                                userMap.put("address", faddress);
                                                userMap.put("gender", gen.getSelectedItem().toString());
                                                userMap.put("cid", CustomerOnlineID);
                                                userMap.put("status", "Active");
                                                userMap.put("password", pas);
                                                userMap.put("email", em);
                                                CustomerDatabaseRef.updateChildren(userMap);
                                               Intent intent = new Intent(register_customer.this, MainActivity.class);
                                              startActivity(intent);
                                                finish();
                                                Toast.makeText(register_customer.this, "Passenger Registered.",
                                                        Toast.LENGTH_SHORT).show();
                                                loadingbar.dismiss();


                                            } else {
                                                Toast.makeText(register_customer.this, "Customer Registration Error." + task.getException().toString(),
                                                        Toast.LENGTH_SHORT).show();
                                                loadingbar.dismiss();
                                            }
                                        }
                                    });
                        }
                    } else {
                        new SweetAlertDialog(register_customer.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Password")
                                .setContentText("password not match")
                                .setConfirmText("Ok")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                    }
                                }).show();
                    }
                } else {
                    new SweetAlertDialog(register_customer.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Password")
                            .setContentText("password must be at least 6 character")
                            .setConfirmText("Ok")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                }
                            }).show();

                }

            }

        });
        btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  Intent intent = new Intent(register_customer.this, logincustomer.class);
              //  startActivity(intent);
               // finish();
            }
        });
        check_terms();
    }

    public void Register_Customer(View v) {
        //  Intent intent = new Intent(logincustomer.this, TryPhone.class);
        //  startActivity(intent);
        //  finish();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://trikila.ga/terms.html"));
        startActivity(browserIntent);
        return;
    }

    private void check_terms() {
        if(checkBox.isChecked()){
            btnreg.setEnabled(true);
        }
        else {
            btnreg.setEnabled(false);
        }

    }
}
