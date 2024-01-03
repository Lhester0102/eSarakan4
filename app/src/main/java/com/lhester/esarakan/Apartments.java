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
import android.net.Uri;
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
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Apartments extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference CustomerDatabaseRef;
    private String CustomerOnlineID;
    public Button btncancel, addlist;
    public EditText building, room_no, available, price, info;
    public static EditText address;
    public Spinner type, gender;
    private ImageView listingImage,permitimage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apartments);
        check_permision();
        storageProfilePicRef = FirebaseStorage.getInstance().getReference().child("Listing_Images");
        mAuth = FirebaseAuth.getInstance();
        info = findViewById(R.id.info);
        btncancel = findViewById(R.id.btncancel);
        addlist = findViewById(R.id.add_listing);
        building = findViewById(R.id.building);
        room_no = findViewById(R.id.room_no);
        address = findViewById(R.id.address);
        type = findViewById(R.id.ltype);
        gender = findViewById(R.id.fortype);
        available = findViewById(R.id.available);
        price = findViewById(R.id.price);
        info = findViewById(R.id.info);
        listingImage = findViewById(R.id.buidling_image);
        permitimage = findViewById(R.id.permit);

        address.setOnClickListener(view -> {
            startActivity(new Intent(Apartments.this, Place_Pickup.class));
        });
        listingImage.setOnClickListener(view -> {
            crop_image();
        });
        permitimage.setOnClickListener(view -> {
            crop_image2();
        });
        addlist.setOnClickListener(v -> {
            if ((uri != null)&& uri2!=null) {
                uploadImage();

            } else {
                new SweetAlertDialog(Apartments.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error Listings")
                        .setContentText("No Selected Image?")
                        .setConfirmText("Ok")
                        .setConfirmClickListener(sDialog -> {
                            sDialog.dismissWithAnimation();
                        })
                        .show();
            }
            //  addListingInfo();
        });
        btncancel.setOnClickListener(view -> {
            Intent intent = new Intent(Apartments.this, Lisitngs.class);
            startActivity(intent);
            finish();
        });

    }

    private void addListingInfo(Uri downUri,Uri downUri2, String key) {
        CustomerOnlineID = mAuth.getCurrentUser().getUid();
        CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference()
                .child("Listings").child(key);
        CustomerDatabaseRef.setValue(true);
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("ID", CustomerOnlineID);
        userMap.put("Building_ID", key);
        userMap.put("Building", building.getText().toString());
        userMap.put("room_no", room_no.getText().toString());
        userMap.put("address", address.getText().toString());
        userMap.put("addressLat", Place_Pickup.Listing_Location.getLatitude());
        userMap.put("addressLng", Place_Pickup.Listing_Location.getLongitude());
        userMap.put("available", available.getText().toString());
        userMap.put("price", price.getText().toString());
        userMap.put("info", info.getText().toString());
        userMap.put("type", type.getSelectedItem().toString());
        userMap.put("gender", gender.getSelectedItem().toString());
        userMap.put("image1", downUri.toString());
        userMap.put("permit", downUri2.toString());
        userMap.put("status", "Unverified");

        CustomerDatabaseRef.updateChildren(userMap);

        Toast.makeText(Apartments.this, "Listing Added!.",
                Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(Apartments.this, Lisitngs.class);
        startActivity(intent);
        finish();
    }

    private StorageReference storageProfilePicRef;
    private String myUri = "";

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Setting Up Listing");
        progressDialog.setMessage("Please wait...saving your listing..");
        progressDialog.setCancelable(false);
        progressDialog.show();
        CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference();
        String key = CustomerDatabaseRef.child("Listings").push().getKey();
        final StorageReference ref = storageProfilePicRef.child(key + ".jpg");
        final UploadTask uploadTask = ref.putBytes(downsizedImageBytes);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Toast.makeText(Apartments.this, "Uploaded", Toast.LENGTH_SHORT).show();

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
                                    // Log.d("Final URL", "onComplete: Url: " + downUri.toString());
                                    uploadImage2(downUri, key);

//                                   if(uri2!=null){
//                                       uploadImage2(key);
//                                   }
//                                    if(uri3!=null){
//                                        uploadImage3(key);
//                                    }
                                    progressDialog.dismiss();
                                }
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double current_progress = (snapshot.getBytesTransferred() * 100) / snapshot.getTotalByteCount();
                        progressDialog.setMessage("Uploading Default Image " + String.format("%.2f", current_progress) + "%");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(Apartments.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadImage2(Uri downUri, String key) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Setting Up Listing");
        progressDialog.setMessage("Please wait...saving your listing..");
        progressDialog.setCancelable(false);
        progressDialog.show();
        CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference();
        final StorageReference ref = storageProfilePicRef.child(key+"permit.jpg");
        final UploadTask uploadTask = ref.putBytes(downsizedImageBytes2);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Toast.makeText(Apartments.this, "Uploaded", Toast.LENGTH_SHORT).show();

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
                                    Uri downUri2 = task.getResult();
                                    // Log.d("Final URL", "onComplete: Url: " + downUri.toString());
                                    addListingInfo(downUri,downUri2, key);
                                    progressDialog.dismiss();
                                }
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double current_progress = (snapshot.getBytesTransferred() * 100) / snapshot.getTotalByteCount();
                        progressDialog.setMessage("Uploading Permit " + String.format("%.2f", current_progress) + "%");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(Apartments.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private int REQ_CODE = 1;
    Uri uri, uri2;
    byte[] downsizedImageBytes,downsizedImageBytes2;

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
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 2);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean b = resultCode == RESULT_OK;
        if (requestCode == REQ_CODE) {
            uri = data.getData();
            int scaleDivider = 4;
            if (null != uri) {
                try {
                    Bitmap fullBitmap = MediaStore.Images.Media.getBitmap(Apartments.this.getApplicationContext().getContentResolver(), uri);
                    int scaleWidth = fullBitmap.getWidth() / scaleDivider;
                    int scaleHeight = fullBitmap.getHeight() / scaleDivider;
                    downsizedImageBytes = getDownsizedImageBytes(fullBitmap, scaleWidth, scaleHeight);
                    listingImage.setImageURI(uri);
                } catch (IOException ioEx) {
                    ioEx.printStackTrace();
                }
            }
        }
        if (requestCode == 2) {
            uri2 = data.getData();
            int scaleDivider = 4;
            if (null != uri2) {
                try {
                    Bitmap fullBitmap = MediaStore.Images.Media.getBitmap(Apartments.this.getApplicationContext().getContentResolver(), uri2);
                    int scaleWidth = fullBitmap.getWidth() / scaleDivider;
                    int scaleHeight = fullBitmap.getHeight() / scaleDivider;
                    downsizedImageBytes2 = getDownsizedImageBytes(fullBitmap, scaleWidth, scaleHeight);
                    permitimage.setImageURI(uri2);
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

    private void check_permision() {
        if (ActivityCompat.checkSelfPermission(Apartments.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(Apartments.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Apartments
                    .this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if (ActivityCompat.checkSelfPermission(Apartments.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(Apartments.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Apartments
                    .this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
        if (ActivityCompat.checkSelfPermission(Apartments.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(Apartments.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Apartments
                    .this, new String[]{Manifest.permission.CAMERA}, 4);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // check whether storage permission granted or not.
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // do what you want;
                    }
                }
                break;
            case 2:
                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // check whether storage permission granted or not.
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // do what you want;
                    }
                }
            case 4:
                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.CAMERA)) {
                    // check whether storage permission granted or not.
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // do what you want;
                    }
                }
                break;
            default:
                break;
        }
    }
}