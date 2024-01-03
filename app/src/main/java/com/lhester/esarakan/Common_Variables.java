package com.lhester.esarakan;

//import com.mapbox.mapboxsdk.geometry.LatLng;

import android.content.Context;
import android.content.Intent;

public class Common_Variables {
    public static String pn="";
    public  static  String Listing_ID="";
    //public static String Lessor_ID="";
    public static String Lessee_ID="";
    public static String MessageID="";
    public static int message_count=0;
    //public static com.mapbox.mapboxsdk.geometry.LatLng Listing_Location;
    public static void backMain(Context context){
        context.startActivity(new Intent(context, MainActivity.class));
    }
}
