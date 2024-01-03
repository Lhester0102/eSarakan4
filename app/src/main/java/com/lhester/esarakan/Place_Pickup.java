package com.lhester.esarakan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;

import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;


import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Place_Pickup extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {
    private static final String TAG = "DirectionsActivity";
    private static final int PLACE_SELECTION_REQUEST_CODE = 56789;
    final int REQ_PERMISSION = 1;
    com.mapbox.mapboxsdk.annotations.Marker marker_des;
    Marker Listing_Marker;
    private com.mapbox.mapboxsdk.geometry.LatLng latLng = null;
    private LocationComponent locationComponent;
    // variables for calculating and drawing a route
    private DirectionsRoute currentRoute;
    private NavigationMapRoute navigationMapRoute;
    private MapboxMap mapboxMap;

    private MapView MyMapView;
    private com.mapbox.mapboxsdk.annotations.Marker passengerMarker, destinationMarkerr;
    // Variables needed to handle location permissions
    private PermissionsManager permissionsManager;
    // Variables needed to add the location engine
    private LocationEngine locationEngine;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 1;
    // Variables needed to listen to location updates
    private MainActivityLocationCallback callback = new MainActivityLocationCallback(Place_Pickup.this);
    public static Location mLastLocation;
    private Button btnSetDes, btnDes, btnsat, btnnorm;
    private TextView desti, pick;
    private String geojsonSourceLayerId = "geojsonSourceLayerId2";
    private String symbolIconId = "symbolIconId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(Place_Pickup.this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_place_pickup);
        MyMapView = findViewById(R.id.mapView);
        MyMapView.onCreate(savedInstanceState);
        MyMapView.getMapAsync(this);
        desti = findViewById(R.id.txt_destination);
        btnnorm = findViewById(R.id.button2);

        btnsat = findViewById(R.id.button3);
        btnsat.setOnClickListener(v -> {
            mapboxMap.setStyle(Style.SATELLITE_STREETS);
        });
        btnnorm.setOnClickListener(v -> {
            mapboxMap.setStyle(Style.MAPBOX_STREETS);
           // startActivity(new Intent(Place_Pickup.this,MainActivity.class));
        });
        desti.setOnClickListener(v -> {
            //   mapboxMap.setStyle(Style.MAPBOX_STREETS);
            Intent intent = new PlaceAutocomplete.IntentBuilder()
                    .accessToken("sk.eyJ1IjoibGhlc3RlcjA4IiwiYSI6ImNsOHY3MzZ4ajBiNXczb21wdmR0eDQ2amQifQ.KXpabXOnOoqOpkBYqYNQSA")
                    .placeOptions(PlaceOptions.builder()
                            .backgroundColor(Color.parseColor("#EEEEEE"))
                            .limit(10)
                            .country("PH")
                            //.addInjectedFeature(home)
                            //.addInjectedFeature(work)
                            .build(PlaceOptions.MODE_CARDS))
                    .build(Place_Pickup.this);
            startActivityForResult(intent, PLACE_SELECTION_REQUEST_CODE);
        });
        btnSetDes = findViewById(R.id.btnSetDes);
        btnSetDes.setOnClickListener(v -> {
            LatLng latLng = mapboxMap.getProjection().getVisibleRegion().latLngBounds.getCenter();
            if (Listing_Marker != null) {
                Listing_Marker.remove();
            }
            Listing_Marker = mapboxMap.addMarker(new MarkerOptions().position(latLng).title("Selected Area Search"));
            Listing_Location = new LatLng(latLng);
            Geocoder geocoder2 = new Geocoder(Place_Pickup.this, Locale.getDefault());
            String pick_address = "";
            try {
                List<Address> addresses = geocoder2.getFromLocation(latLng.getLatitude(), latLng.getLongitude(), 1);
                pick_address = addresses.get(0).getAddressLine(0);
               // desti.setText(pick_address);
               Apartments.address.setText(pick_address);
            } catch (IOException e) {
                e.printStackTrace();
            }
            finish();
        });
    }

    public static com.mapbox.mapboxsdk.geometry.LatLng Listing_Location;
    // Check for permission to access Location
    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(Place_Pickup.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    // Asks for permission
    private void askPermission() {
        ActivityCompat.requestPermissions(
                Place_Pickup.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQ_PERMISSION
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    if (checkPermission())
                        MyMapView.getMapAsync(Place_Pickup.this);

                } else {
                    // Permission denied
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Place_Pickup.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //  mGoogleApiClient.disconnect();
        //   LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        //  Common_Variables.customer_pickuploaction = null;
        // NewRide.fr.setText("");
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_SELECTION_REQUEST_CODE) {
            double lat = 0, lng = 0;
            LatLng LATLNG = null;
            if (resultCode == RESULT_OK) {
                CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

                if (mapboxMap != null) {
                    Style style = mapboxMap.getStyle();
                    if (style != null) {
                        GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
                        if (source != null) {
                            source.setGeoJson(FeatureCollection.fromFeatures(
                                    new Feature[]{Feature.fromJson(selectedCarmenFeature.toJson())}));
                        }
                        lat = ((Point) selectedCarmenFeature.geometry()).latitude();
                        lng = ((Point) selectedCarmenFeature.geometry()).longitude();
                        LATLNG = new com.mapbox.mapboxsdk.geometry.LatLng(lat, lng);

                        Location loc1 = new Location("");
                        loc1.setLatitude(mLastLocation.getLatitude());
                        loc1.setLongitude(mLastLocation.getLongitude());

                        Location loc2 = new Location("");
                        loc2.setLatitude(lat);
                        loc2.setLongitude(lng);

                        if ((loc1.distanceTo(loc2) / 1000) <= 50) {
                            btnSetDes.setEnabled(true);
                            lat = ((Point) selectedCarmenFeature.geometry()).latitude();
                            lng = ((Point) selectedCarmenFeature.geometry()).longitude();
                            LATLNG = new LatLng(lat, lng);
// Move map camera to the selected location
                            mapboxMap.animateCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLngZoom(LATLNG, 17));
                        } else {
                            Toast.makeText(this, "Exceeding distance Limit", Toast.LENGTH_SHORT).show();
                            btnSetDes.setEnabled(false);
                            if (navigationMapRoute != null) {
                                navigationMapRoute.removeRoute();
                            }
                        }
                    }
                }
                Geocoder geocoder = new Geocoder(Place_Pickup.this, Locale.getDefault());
                String address = "";
                try {
                    List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                    address = addresses.get(0).getAddressLine(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                desti.setText(address);
                //  Common_Variables.customer_pickuploaction = LATLNG;
            }

            //    destination_Marker = mMap.addMarker(new MarkerOptions().position(Common_Variables.customer_destination).title("Selected Destination"));
            //  mMap.moveCamera(CameraUpdateFactory.newLatLng(Common_Variables.customer_destination));
            //  mMap.animateCamera(CameraUpdateFactory.zoomTo(20));

        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(Place_Pickup.this, status.getStatusMessage(), Toast.LENGTH_LONG);
        } else if (resultCode == RESULT_CANCELED) {
        }
        return;
    }


    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.MAPBOX_STREETS,
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                        mapboxMap.getUiSettings().setAttributionEnabled(false);
                        mapboxMap.getUiSettings().setLogoEnabled(false);
                        //   addDestinationIconSymbolLayer(style);
                        if (mLastLocation==null)
                        {
                            return;
                        }
                        double locationLat = mLastLocation.getLatitude();
                        double locationLng = mLastLocation.getLongitude();
                        LatLng latLng=new LatLng(locationLat,locationLng);
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(latLng)      // Sets the center of the map to Mountain View
                                .zoom(17)                   // Sets the zoom
                             //   .bearing(90)                // Sets the orientation of the camera to east
                             //   .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                                .build();                   // Creates a CameraPosition from the builder

                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }
                });
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "R.string.user_location_permission_explanation", Toast.LENGTH_LONG).show();
    }

    /**
     * Initialize the Maps SDK's LocationComponent
     */
    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

// Get an instance of the component
            locationComponent = mapboxMap.getLocationComponent();

// Set the LocationComponent activation options
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .useDefaultLocationEngine(false)
                            .build();

// Activate with the LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(locationComponentActivationOptions);

// Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

// Set the component's camera mode
            //     locationComponent.setCameraMode(CameraMode.TRACKING);

// Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */
    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            if (mapboxMap.getStyle() != null) {
                enableLocationComponent(mapboxMap.getStyle());
            }
        } else {
            Toast.makeText(this, "Not Granted", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void getRoute(Point origin, Point destination) {
        assert Mapbox.getAccessToken() != null;
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        // You can get the generic HTTP info about the response
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e(TAG, "No routes found");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);

                        // Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, MyMapView, mapboxMap, com.mapbox.services.android.navigation.ui.v5.R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });
    }

    private class MainActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<Place_Pickup> activityWeakReference;

        MainActivityLocationCallback(Place_Pickup activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        @Override
        public void onSuccess(LocationEngineResult result) {
            Place_Pickup activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();
                mLastLocation = location;

                if (location == null) {
                    return;
                }

                if (getApplicationContext() != null) {
                    mLastLocation = location;
                    com.mapbox.mapboxsdk.geometry.LatLng latLng = new com.mapbox.mapboxsdk.geometry.LatLng(location.getLatitude(), location.getLongitude());

                    // locationComponent.setCameraMode(CameraMode.TRACKING);
                }

                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can not be captured
         *
         * @param exception the exception message
         */
        @Override
        public void onFailure(@NonNull Exception exception) {
            Log.d("LocationChangeActivity", exception.getLocalizedMessage());
            Place_Pickup activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}