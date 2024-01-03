package com.lhester.esarakan;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class Msgs extends AppCompatActivity {
    private Button btnsend;
    private EditText txtmsg;
    private DatabaseReference msgRef,msgRef2;
    ListView lv;
    private DatabaseReference databaseReference,msgreference;
    ArrayList<HashMap<String, String>> lists;
    private SimpleAdapter adapter;
    private TextView t1, t2, t3, t4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msgs);
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Messages")
                .child(Common_Variables.MessageID);
        msgreference = FirebaseDatabase.getInstance().getReference()
                .child("Messages");

        myID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        lv = findViewById(R.id.listsmsgs);
        getLists();
        update_seen(Common_Variables.Lessee_ID);
        check_message_count();
        lists = new ArrayList<HashMap<String, String>>();
        adapter = new SimpleAdapter(Msgs.this, lists, R.layout.msglists,
                new String[]{"messageC", "messageD"},
                new int[]{R.id.sent, R.id.received}
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View itemView = super.getView(position, convertView, parent);
                HashMap<String, Object> obj = (HashMap<String, Object>) adapter.getItem(position);

                if ((String) obj.get("messageD") != "") {
                    itemView.findViewById(R.id.received).setBackgroundResource(R.drawable.messagec);
                    itemView.findViewById(R.id.received).setVisibility(View.VISIBLE);
                    itemView.findViewById(R.id.sent).setVisibility(View.GONE);

                }
                if ((String) obj.get("messageC") != "") {
                    itemView.findViewById(R.id.sent).setBackgroundResource(R.drawable.messaged);
                    itemView.findViewById(R.id.sent).setVisibility(View.VISIBLE);
                    itemView.findViewById(R.id.received).setVisibility(View.GONE);
                }
                return itemView;
            }
        };

        lv.setAdapter(adapter);
        btnsend = findViewById(R.id.btnsend);
        txtmsg = findViewById(R.id.txtmsg);
        btnsend.setOnClickListener(v -> {
            msgRef = FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child("Seekers")
                    .child(Common_Variables.MessageID)
                    .child("Messages");

            msgRef2 = FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child("Customers")
                    .child(myID)
                    .child("Messages");

            DateFormat dfgmt = new java.text.SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
            dfgmt.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
            String timteStamp = dfgmt.format(new Date());
            String userkey = msgRef.push().getKey();
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("messageC", txtmsg.getText().toString());
            userMap.put("SID",Common_Variables.MessageID );
            userMap.put("LID", FirebaseAuth.getInstance().getCurrentUser().getUid());
            userMap.put("ListingID", Common_Variables.Listing_ID);
            userMap.put("key", userkey);
            userMap.put("timeStamp", timteStamp);
            userMap.put("seen", "False");
            DatabaseReference newmsg = FirebaseDatabase.getInstance().getReference()
                    .child("Messages")
                    .child(userkey);
            newmsg.updateChildren(userMap);

            HashMap<String, Object> userMap2 = new HashMap<>();
            userMap2.put("LID", myID);
            userMap2.put("seen", "false");
            userMap2.put("message", txtmsg.getText().toString());
            msgRef.child(myID).updateChildren(userMap2);

//            HashMap<String, Object> userMap3 = new HashMap<>();
//            userMap3.put("LID", myID);
//            msgRef2.child(myID).updateChildren(userMap3);


            txtmsg.setText("");
            Toast.makeText(Msgs.this, "sent", Toast.LENGTH_SHORT).show();
        });
    }

    private void getLists() {
//        lists.clear();

        msgreference.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                        if (postSnapShot.getKey().equals("LID")) {
                            String value1 = dataSnapshot.child("LID").getValue().toString();
                            String value2 = dataSnapshot.child("SID").getValue().toString();
                            if (value1.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) ) {
                                if(value2.equals(Common_Variables.MessageID)) {
                                    // String value2 = dataSnapshot.getKey();
                                    String messageC = "";
                                    String messageD = "";
                                    String tc="";
                                    String td="";
                                    String seen = "";
                                    if (dataSnapshot.child("messageC").exists()) {
                                        messageD = dataSnapshot.child("messageC").getValue().toString();
                                        tc = dataSnapshot.child("timeStamp").getValue().toString();

                                    }
                                    if (dataSnapshot.child("messageD").exists()) {
                                        messageC = dataSnapshot.child("messageD").getValue().toString();
                                        td = dataSnapshot.child("timeStamp").getValue().toString();
                                        if (dataSnapshot.child("seen").exists()) {
                                            seen = dataSnapshot.child("seen").getValue().toString();
                                        }
//                                        if (seen.equals("False")) {
//                                           update_seen(value2);
//
//                                        }
                                    }

                                    HashMap<String, String> datum = new HashMap<String, String>();
                                    //  datum.put("Images", value2);
                                    datum.put("messageD", messageD);
                                    datum.put("messageC", messageC);
                                    lists.add(datum);
                                    adapter.notifyDataSetChanged();
                                }
                            }

                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                adapter.notifyDataSetChanged();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity( new Intent(Msgs.this,MsgLists.class));
        finish();
    }

    private String myID = "";

    private void update_seen(String key) {
        myID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference newmsg = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Customers")
                .child(myID)
                .child("Messages").child(key);
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("seen", "true");
        newmsg.updateChildren(userMap);

    }

    DatabaseReference databaseReference2;
    private void check_message_count(){
        databaseReference2 = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child("Customers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Messages");
        getLists2();
    }

    private void getLists2() {
        databaseReference2.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                        Log.e("postSnapShot seen", postSnapShot.getKey());
                        if (postSnapShot.getKey().equals("seen")) {
                            String value1 = dataSnapshot.child("seen").getValue().toString();
                            Log.e("Value",String.valueOf(Common_Variables.message_count));
                            if(value1.equals("true")){
                                Common_Variables.message_count=Common_Variables.message_count-1;
                                Log.e("msgcount",  String.valueOf( Common_Variables.message_count));

                                MainActivity.badgeDrawable.setNumber(Common_Variables.message_count);
                                if(Common_Variables.message_count<1){
                                    MainActivity.badgeDrawable.setVisible(false);
                                }
                            }
                        }

                    }
                }

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