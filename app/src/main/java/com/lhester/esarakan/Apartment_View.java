package com.lhester.esarakan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Apartment_View extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference CustomerDatabaseRef;
    private String CustomerOnlineID;
    public Button btncancel, updateListing, archive;
    public EditText building, room_no, address, available, price, info;
    public Spinner type, gender;
    public ImageView listingImage, listingImage2, listingImage3;

    String STAT;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apartment_view);
        mAuth = FirebaseAuth.getInstance();
        storageProfilePicRef = FirebaseStorage.getInstance().getReference().child("Listing_Images");
        btncancel = findViewById(R.id.btncancel);
        updateListing = findViewById(R.id.add_listing);
        building = findViewById(R.id.building);
        room_no = findViewById(R.id.room_no);
        address = findViewById(R.id.address);
        type = findViewById(R.id.ltype);
        gender = findViewById(R.id.gender);
        price = findViewById(R.id.price);
        info = findViewById(R.id.info);
        available = findViewById(R.id.available);
        listingImage = findViewById(R.id.buidling_image);

        listingImage2 = findViewById(R.id.buidling_image2);
        listingImage3 = findViewById(R.id.buidling_image3);

        archive = findViewById(R.id.Archive);

        archive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference()
                        .child("Listings").child(Common_Variables.Listing_ID);
                HashMap<String, Object> userMap = new HashMap<>();
                userMap.put("status", STAT);
                CustomerDatabaseRef.updateChildren(userMap);
                Toast.makeText(Apartment_View.this, "Listing has been " + archive.getText().toString(), Toast.LENGTH_SHORT).show();

            }
        });
        listingImage.setOnClickListener(view -> {
            crop_image();
        });
        listingImage2.setOnClickListener(view -> {
            crop_image2();
        });
        listingImage3.setOnClickListener(view -> {
            crop_image3();
        });
        getListingInfo();

        updateListing.setOnClickListener(view -> {
            update_info();
        });
    }

    private void update_info() {
        CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference()
                .child("Listings").child(Common_Variables.Listing_ID);

        //   CustomerDatabaseRef.setValue(true);
        final ProgressDialog progressDialog = new ProgressDialog(Apartment_View.this);
        progressDialog.setTitle("Setting Accounts Info");
        progressDialog.setMessage("Please wait...saving your account..");
        progressDialog.show();

        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("Building", building.getText().toString());
        userMap.put("room_no", room_no.getText().toString());
        userMap.put("address", address.getText().toString());
        userMap.put("available", available.getText().toString());
        userMap.put("price", price.getText().toString());
        userMap.put("info", info.getText().toString());
        userMap.put("type", type.getSelectedItem().toString());
        userMap.put("gender", gender.getSelectedItem().toString());

        CustomerDatabaseRef.updateChildren(userMap);
        Toast.makeText(Apartment_View.this, "Updated", Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }


    private void getListingInfo() {

        Query mCustomerDatabase =
                FirebaseDatabase.getInstance().getReference()
                        .child("Listings")
                        .child(Common_Variables.Listing_ID);
        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e("Found", String.valueOf(dataSnapshot.getChildrenCount()));
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                if (map.get("Building") != null) {
                    String _ratings = dataSnapshot.child("Building").getValue().toString();
                    building.setText(_ratings);
                }
                if (map.get("Type") != null) {
                    String _type = dataSnapshot.child("Type").getValue().toString();
                    type.setPrompt(_type);
                }
                if (map.get("status") != null) {
                    String stat = dataSnapshot.child("status").getValue().toString();
                    if (stat.equals("Available")) {
                        archive.setBackground(getResources().getDrawable(R.drawable.custom_btn_red));
                        archive.setText("Archived");
                        archive.setEnabled(true);
                        STAT="Archive";
                    } else if (stat.equals("Unverified")){
                        archive.setBackground(getResources().getDrawable(R.drawable.btnred_pressed));
                        archive.setText("Under Review");
                        archive.setEnabled(false);
                    }

                    else {
                        archive.setBackground(getResources().getDrawable(R.drawable.btn_other_default));
                        archive.setText("Retrieve");
                        archive.setEnabled(true);
                        STAT="Available";
                    }
                }
                if (map.get("gender") != null) {
                    String _gender = dataSnapshot.child("gender").getValue().toString();
                    type.setPrompt(_gender);
                }
                if (map.get("available") != null) {
                    available.setText(map.get("available").toString());
                }
                if (map.get("price") != null) {
                    price.setText(map.get("price").toString());
                }
                if (map.get("info") != null) {
                    info.setText(map.get("info").toString());
                }
                if (map.get("room_no") != null) {
                    room_no.setText(map.get("room_no").toString());
                }
                if (map.get("address") != null) {
                    address.setText(map.get("address").toString());
                }
                if (map.get("image1") != null) {
                    Picasso.get().load(dataSnapshot.child("image1").getValue().toString())
                            .placeholder(R.drawable.add_image)
                            .error(R.drawable.person)
                            .into(listingImage);
                }
                if (map.get("image2") != null) {
                    Picasso.get().load(dataSnapshot.child("image2").getValue().toString())
                            .placeholder(R.drawable.add_image)
                            .error(R.drawable.person)
                            .into(listingImage2);
                }
                if (map.get("image3") != null) {
                    Picasso.get().load(dataSnapshot.child("image3").getValue().toString())
                            .placeholder(R.drawable.add_image)
                            .error(R.drawable.person)
                            .into(listingImage3);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(Apartment_View.this, Lisitngs.class));
        finish();
    }

    private void crop_image() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQ_CODE);
    }

    private void crop_image2() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQ_CODE2);
    }

    private void crop_image3() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQ_CODE3);
    }

    private int REQ_CODE = 1;
    private int REQ_CODE2 = 2;
    private int REQ_CODE3 = 3;
    byte[] downsizedImageBytes;
    byte[] downsizedImageBytes2;
    byte[] downsizedImageBytes3;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE) {
            uri = data.getData();
            int scaleDivider = 4;
            if (null != uri) {
                try {
                    Bitmap fullBitmap = MediaStore.Images.Media.getBitmap(Apartment_View.this.getApplicationContext().getContentResolver(), uri);
                    int scaleWidth = fullBitmap.getWidth() / scaleDivider;
                    int scaleHeight = fullBitmap.getHeight() / scaleDivider;
                    downsizedImageBytes = getDownsizedImageBytes(fullBitmap, scaleWidth, scaleHeight);
                    listingImage.setImageURI(uri);
                    uploadImage(Common_Variables.Listing_ID);
                } catch (IOException ioEx) {
                    ioEx.printStackTrace();
                }
            }
        }
        if (requestCode == REQ_CODE2) {
            uri2 = data.getData();
            int scaleDivider = 4;
            if (null != uri2) {
                try {
                    Bitmap fullBitmap = MediaStore.Images.Media.getBitmap(Apartment_View.this.getApplicationContext().getContentResolver(), uri2);
                    int scaleWidth = fullBitmap.getWidth() / scaleDivider;
                    int scaleHeight = fullBitmap.getHeight() / scaleDivider;
                    downsizedImageBytes2 = getDownsizedImageBytes(fullBitmap, scaleWidth, scaleHeight);
                    listingImage2.setImageURI(uri2);
                    uploadImage2(Common_Variables.Listing_ID);
                } catch (IOException ioEx) {
                    ioEx.printStackTrace();
                }
            }
        }
        if (requestCode == REQ_CODE3) {
            uri3 = data.getData();
            int scaleDivider = 4;
            if (null != uri3) {
                try {
                    Bitmap fullBitmap = MediaStore.Images.Media.getBitmap(Apartment_View.this.getApplicationContext().getContentResolver(), uri3);
                    int scaleWidth = fullBitmap.getWidth() / scaleDivider;
                    int scaleHeight = fullBitmap.getHeight() / scaleDivider;
                    downsizedImageBytes3 = getDownsizedImageBytes(fullBitmap, scaleWidth, scaleHeight);
                    listingImage3.setImageURI(uri3);
                    uploadImage3(Common_Variables.Listing_ID);
                } catch (IOException ioEx) {
                    ioEx.printStackTrace();
                }
            }
        }
    }

    public byte[] getDownsizedImageBytes(Bitmap fullBitmap, int scaleWidth, int scaleHeight) throws IOException {

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(fullBitmap, scaleWidth, scaleHeight, true);

        // 2. Instantiate the downsized image content as a byte[]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] downsizedImageBytes = baos.toByteArray();

        return downsizedImageBytes;
    }

    Uri uri, uri2, uri3;

    private void uploadImage(String key) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Setting Up Listing");
        progressDialog.setMessage("Please wait...saving your listing..");
        progressDialog.setCancelable(false);
        progressDialog.show();
        final StorageReference ref = storageProfilePicRef.child(key + ".jpg");
        final UploadTask uploadTask = ref.putFile(uri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Toast.makeText(Apartment_View.this, "Uploaded", Toast.LENGTH_SHORT).show();

                        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw Objects.requireNonNull(task.getException());

                                }
                                return ref.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downUri = task.getResult();
                                    CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference()
                                            .child("Listings").child(key);
                                    HashMap<String, Object> userMap = new HashMap<>();
                                    userMap.put("image1", downUri.toString());
                                    CustomerDatabaseRef.updateChildren(userMap);
                                    // Log.d("Final URL", "onComplete: Url: " + downUri.toString());
                                    progressDialog.dismiss();
                                }
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double current_progress = (snapshot.getBytesTransferred() * 100) / snapshot.getTotalByteCount();
                        progressDialog.setMessage("Uploading " + String.format("%.2f", current_progress) + "%");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(Apartment_View.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadImage2(String key) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Setting Up Listing");
        progressDialog.setMessage("Please wait...saving your listing..");
        progressDialog.setCancelable(false);
        progressDialog.show();
        final StorageReference ref = storageProfilePicRef.child(key + "2.jpg");
        final UploadTask uploadTask = ref.putFile(uri2);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Toast.makeText(Apartment_View.this, "Uploaded", Toast.LENGTH_SHORT).show();

                        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw Objects.requireNonNull(task.getException());

                                }
                                return ref.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downUri = task.getResult();
                                    CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference()
                                            .child("Listings").child(key);
                                    HashMap<String, Object> userMap = new HashMap<>();
                                    userMap.put("image2", downUri.toString());
                                    CustomerDatabaseRef.updateChildren(userMap);
                                    // Log.d("Final URL", "onComplete: Url: " + downUri.toString());
                                    progressDialog.dismiss();
                                }
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double current_progress = (snapshot.getBytesTransferred() * 100) / snapshot.getTotalByteCount();
                        progressDialog.setMessage("Uploading " + String.format("%.2f", current_progress) + "%");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(Apartment_View.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private StorageReference storageProfilePicRef;

    private void uploadImage3(String key) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Setting Up Listing");
        progressDialog.setMessage("Please wait...saving your listing..");
        progressDialog.setCancelable(false);
        progressDialog.show();
        final StorageReference ref = storageProfilePicRef.child(key + "3.jpg");
        final UploadTask uploadTask = ref.putFile(uri3);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Toast.makeText(Apartment_View.this, "Uploaded", Toast.LENGTH_SHORT).show();

                        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw Objects.requireNonNull(task.getException());

                                }
                                return ref.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downUri = task.getResult();
                                    CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference()
                                            .child("Listings").child(key);
                                    HashMap<String, Object> userMap = new HashMap<>();
                                    userMap.put("image3", downUri.toString());
                                    CustomerDatabaseRef.updateChildren(userMap);
                                    progressDialog.dismiss();
                                }
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double current_progress = (snapshot.getBytesTransferred() * 100) / snapshot.getTotalByteCount();
                        progressDialog.setMessage("Uploading " + String.format("%.2f", current_progress) + "%");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(Apartment_View.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}