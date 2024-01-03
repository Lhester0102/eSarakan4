package com.lhester.esarakan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Lisitngs extends AppCompatActivity {
public Button AddListing;
    private FloatingActionButton fab;
    ListView lv;
    ArrayList<HashMap<String, String>> lists;
    private SimpleAdapter adapter;
    private Query databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lisitngs);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Listings");
        AddListing=findViewById(R.id.add_listing);
        lv=findViewById(R.id.listings);
        fab=findViewById(R.id.fab);
        getLists();
        lists = new ArrayList<HashMap<String, String>>();
        // Collections.reverse(lists);

        adapter = new SimpleAdapter(Lisitngs.this, lists, R.layout.lists_layout,
                new String[]{"image","Transaction_ID", "Content", "DT","Price"},
                new int[]{R.id.imageView1ID,R.id.TID, R.id.child, R.id.DT,R.id.price}){
            @SuppressLint("WrongViewCast")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View itemView = super.getView(position, convertView, parent);
                HashMap<String, Object> obj = (HashMap<String, Object>) adapter.getItem(position);
                String uri=  obj.get("image").toString();
                if (obj.get("image")!="") {
                    //   itemView.findViewById(R.id.received).setBackgroundResource(R.drawable.messagec);

                    Picasso.get().load(uri)
                            .resize(200,200)
                            .centerCrop()
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
                Common_Variables.Listing_ID = (String) obj.get("Transaction_ID");
                startActivity(new Intent(Lisitngs.this, Apartment_View.class));
                finish();
                //  Log.e("Yourtag", result);
            }
        });
        AddListing.setOnClickListener(view -> {
            Intent intent = new Intent(Lisitngs.this, Apartments.class);
            startActivity(intent);
            finish();

        });
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(Lisitngs.this, Apartments.class);
            startActivity(intent);
            finish();

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
                        if (postSnapShot.getKey().equals("ID")) {
                            Log.e("postSnapShot",postSnapShot.getKey().toString());
                            String value1 = dataSnapshot.child("ID").getValue().toString();
                            if (value1.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                String Building = dataSnapshot.child("Building").getValue().toString();
                                String Room = dataSnapshot.child("room_no").getValue().toString();
                                String Type = dataSnapshot.child("type").getValue().toString();
                                String price = dataSnapshot.child("price").getValue().toString();
                                String image = dataSnapshot.child("image1").getValue().toString();
                                String available = dataSnapshot.child("available").getValue().toString();
                                HashMap<String, String> datum = new HashMap<String, String>();
                                datum.put("DT",  Building);
                                datum.put("Transaction_ID", dataSnapshot.child("Building_ID").getValue().toString());
                                datum.put("Content", " Available:" + available + " | Room:" + Room +"| Type:" + Type);
                                datum.put("image", image);
                                datum.put("Price", price + "/month");
                                lists.add(datum);
                                adapter.notifyDataSetChanged();
                            }
                            }
                    }

                    // adapter.notifyDataSetChanged();
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
}