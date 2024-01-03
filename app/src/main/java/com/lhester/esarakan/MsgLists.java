package com.lhester.esarakan;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MsgLists extends AppCompatActivity {
    public DrawerLayout drawerLayout;
    Toolbar toolbar;
    BottomNavigationView navigationView;
    //  public ActionBarDrawerToggle actionBarDrawerToggle;

    private FloatingActionButton fab;
    ListView lv;
    ArrayList<HashMap<String, String>> lists;
    private SimpleAdapter adapter;
    private Query databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_lists);
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child("Customers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Messages");
        lv = findViewById(R.id.listings);
        fab = findViewById(R.id.fab);

        getLists();
        lists = new ArrayList<HashMap<String, String>>();
        // Collections.reverse(lists);

        adapter = new SimpleAdapter(MsgLists.this, lists, R.layout.lists_msgs_layout,
                new String[]{"image", "Transaction_ID", "Content", "DT"},
                new int[]{R.id.imageView1ID, R.id.TID, R.id.child, R.id.DT}) {
            @SuppressLint("WrongViewCast")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View itemView = super.getView(position, convertView, parent);
                HashMap<String, Object> obj = (HashMap<String, Object>) adapter.getItem(position);
                String uri = obj.get("image").toString();
                if (obj.get("image") != "") {
                    Picasso.get().load(uri).resize(100, 100).centerInside()
                            .placeholder(R.drawable.add_image)
                            .error(R.drawable.person)
                            .into((ImageView) itemView.findViewById(R.id.imageView1ID));
                }
                return itemView;
            }
        };

        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(Activity_Histories.this, "Clicked:" + adapter.getItem(position).toString(), Toast.LENGTH_SHORT).show();
                HashMap<String, Object> obj = (HashMap<String, Object>) adapter.getItem(position);
                Common_Variables.MessageID = (String) obj.get("Transaction_ID");
                Common_Variables.Lessee_ID = (String) obj.get("Transaction_ID");
                startActivity(new Intent(MsgLists.this, Msgs.class));
                finish();
                //  Log.e("Yourtag", result);
            }
        });
    }

    private void getLists() {
        if (lists != null) {
            lists.clear();
            adapter.notifyDataSetChanged();
        }
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                        Log.e("postSnapShot LID", postSnapShot.getKey());
                        if (postSnapShot.getKey().equals("SID")) {
                            String value1 = dataSnapshot.child("SID").getValue().toString();
                            String message = dataSnapshot.child("message").getValue().toString();
                            Log.e("Value", value1);
                            getInfo(value1,message);

                        }
                    }
                }
                Collections.reverse(lists);

            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    DatabaseReference databaseReference2;
    String fullname = "", lastname = "", phone = "", email = "", img;
    private void getInfo(String value1,String message) {
        databaseReference2 = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child("Seekers")
                .child(value1);
      //  Log.e("VAluess ",value1);
        databaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    HashMap<String, String> datum = new HashMap<String, String>();
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 2) {
                        if (dataSnapshot.hasChild("first_name")) {
                            fullname = dataSnapshot.child("first_name").getValue().toString();
                        }
                        if (dataSnapshot.hasChild("last_name")) {
                            lastname = dataSnapshot.child("last_name").getValue().toString();

                        }
                        if (dataSnapshot.hasChild("phone")) {
                            phone = dataSnapshot.child("phone").getValue().toString();

                        }
                        if (dataSnapshot.hasChild("email")) {
                            email = dataSnapshot.child("email").getValue().toString();

                        }
                        if (dataSnapshot.hasChild("image")) {
                            img = dataSnapshot.child("image").getValue().toString();
                        }
                        datum.put("DT", fullname + " " + lastname);
                        datum.put("Transaction_ID", dataSnapshot.child("cid").getValue().toString());
                        datum.put("Content", message);
                        datum.put("image", img);
                        lists.add(datum);
                        adapter.notifyDataSetChanged();
                        Log.e("Name ",fullname);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}