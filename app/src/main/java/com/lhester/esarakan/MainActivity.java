package com.lhester.esarakan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collections;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {
    public DrawerLayout drawerLayout;
    Toolbar toolbar;
    public static BadgeDrawable badgeDrawable;
    BottomNavigationView navigationView;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigations();
        badgeDrawable=navigationView.getOrCreateBadge(R.id.msgs);
        badgeDrawable.setVisible(false);
        check_message_count();


    }

    // override the onOptionsItemSelected()
    // function to implement
    // the item click listener callback
    // to open and close the navigation
    // drawer when the icon is clicked
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Log out")
                .setContentText("You sure you want to log out?")
                .setConfirmText("Logout")
                .setConfirmClickListener(sDialog -> {
                    FirebaseAuth.getInstance().signOut();
                    finish();
                    Intent i = new Intent(MainActivity.this, logincustomer.class);
                    //  i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    sDialog.dismissWithAnimation();
                })
                .setCancelButton("Cancel", sDialog -> sDialog.dismissWithAnimation())
                .show();

    }
    private  void lOut(){
        new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Log out")
                .setContentText("You sure you want to log out?")
                .setConfirmText("Logout")
                .setConfirmClickListener(sDialog -> {
                    FirebaseAuth.getInstance().signOut();
                    finish();
                    Intent i = new Intent(MainActivity.this, logincustomer.class);
                    //  i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                 //   drawerLayout.closeDrawers();
                    sDialog.dismissWithAnimation();
                })
                .setCancelButton("Cancel", sDialog -> sDialog.dismissWithAnimation())
                .show();
    }
    private void navigations() {
        navigationView = findViewById(R.id.nav_view);

        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.nav_account:
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        break;
                    case R.id.listing:
                        startActivity(new Intent(MainActivity.this, Lisitngs.class));
                        break;
                    case R.id.msgs:
                        startActivity(new Intent(MainActivity.this, MsgLists.class));
                        break;
                    case R.id.nav_logout:
                        new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Log out")
                                .setContentText("You sure you want to log out?")
                                .setConfirmText("Logout")
                                .setConfirmClickListener(sDialog -> {
                                    FirebaseAuth.getInstance().signOut();
                                    finish();
                                    Intent i = new Intent(MainActivity.this, logincustomer.class);
                                    //  i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);
                                    sDialog.dismissWithAnimation();
                                })
                                .setCancelButton("Cancel", sDialog -> sDialog.dismissWithAnimation())
                                .show();
                        break;
                    default:
                        return true;
                }
                return true;
            }
        });
//        navigationView.setNavigationItemSelectedListener(menuItem -> {
//            int id = menuItem.getItemId();
//            switch (id) {
//                case R.id.nav_account:
//                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
//                    drawerLayout.closeDrawers();
//                    break;
//    /*            case R.id.histories:
//                    startActivity(new Intent(OnlineDrivers.this, Activity_Histories.class));
//                    drawerLayout.closeDrawers();
//                    break;
//                case R.id.sched:
//                    startActivity(new Intent(OnlineDrivers.this, MySchedules.class));
//                    drawerLayout.closeDrawers();
//                    break;
//                case R.id.announcement:
//                    startActivity(new Intent(OnlineDrivers.this, Announcement.class));
//                    drawerLayout.closeDrawers();
//                    break; */
//                case R.id.listing:
//                    startActivity(new Intent(MainActivity.this, Lisitngs.class));
//                    drawerLayout.closeDrawers();
//                    break;
//                case R.id.nav_logout:
//                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
//                            .setTitleText("Log out")
//                            .setContentText("You sure you want to log out?")
//                            .setConfirmText("Logout")
//                            .setConfirmClickListener(sDialog -> {
//                                FirebaseAuth.getInstance().signOut();
//                                finish();
//                                Intent i = new Intent(MainActivity.this, logincustomer.class);
//                                //  i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                startActivity(i);
//                                drawerLayout.closeDrawers();
//                                sDialog.dismissWithAnimation();
//                            })
//                            .setCancelButton("Cancel", sDialog -> sDialog.dismissWithAnimation())
//                            .show();
//                    break;
//                default:
//                    return true;
//            }
//            return true;
//        });
    }
   DatabaseReference databaseReference;
   private void check_message_count(){
       databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child("Customers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Messages");
        getLists();
    }

    private void getLists() {
        Common_Variables.message_count=0;
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                        Log.e("postSnapShot seen", postSnapShot.getKey());
                        if (postSnapShot.getKey().equals("seen")) {
                            String value1 = dataSnapshot.child("seen").getValue().toString();
                            Log.e("Value",String.valueOf(Common_Variables.message_count));
                            if(value1.equals("false")){
                                Common_Variables.message_count=Common_Variables.message_count+1;
                                Log.e("msgcount",  String.valueOf( Common_Variables.message_count));
                                if(Common_Variables.message_count>0) {
                                    badgeDrawable.setVisible(true);
                                    badgeDrawable.setNumber(Common_Variables.message_count);
                                }
                                else {
                                    badgeDrawable.setVisible(false);
                                }

                            }
                        }

                    }
                }

            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Common_Variables.message_count=0;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                        Log.e("postSnapShot seen", postSnapShot.getKey());
                        if (postSnapShot.getKey().equals("seen")) {
                            String value1 = dataSnapshot.child("seen").getValue().toString();
                            Log.e("Value",String.valueOf(Common_Variables.message_count));
                            if(value1.equals("false")){
                                Common_Variables.message_count=Common_Variables.message_count+1;
                                Log.e("msgcount",  String.valueOf( Common_Variables.message_count));
                                if(Common_Variables.message_count>0) {
                                    badgeDrawable.setVisible(true);
                                    badgeDrawable.setNumber(Common_Variables.message_count);
                                }
                                else {
                                    badgeDrawable.setVisible(false);
                                }

                            }
                        }

                    }
                }
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
    protected void onResume() {
        super.onResume();
        Common_Variables.message_count=0;
        check_message_count();
    }

}