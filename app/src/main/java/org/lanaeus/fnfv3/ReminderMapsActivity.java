package org.lanaeus.fnfv3;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReminderMapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener{

    private Toolbar mToolbar;

    private GoogleMap mMap;
    private AutoCompleteTextView mSearchText;

    //Play services location
    private static final int MY_PERMISSION_REQUEST_CODE = 7192;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 300193;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient, cGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 5000; // 5 seconds update interval
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference mLocationDatabase;
    GeoFire geofire;
    FirebaseUser mCurrentUser;

    Marker mCurrentMarker;

    private static final String CHANNEL_ID = "area";
    private static final String CHANNEL_NAME = "REMINDER";

    Circle mapCircle;

    private ImageView mGps;

    private PlaceAutocompleteAdapter placeAutocompleteAdapter;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40,-168), new LatLng(71,136));
    private PlaceInfo mPlace;

    private String mReminderText;

    private ImageButton mSetReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mToolbar = (Toolbar) findViewById(R.id.reminder_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Set Location Reminder");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSetReminder = (ImageButton) findViewById(R.id.fabReminder);

        mReminderText = getIntent().getStringExtra("reminder");

        mSearchText = (AutoCompleteTextView) findViewById(R.id.map_search);
        mGps = (ImageView) findViewById(R.id.ic_gps);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String curr_id = mCurrentUser.getUid();

        mLocationDatabase = FirebaseDatabase.getInstance().getReference().child("Geo").child(curr_id);
        geofire = new GeoFire(mLocationDatabase);

        setUpLocation();

        final String searchText = mSearchText.getText().toString();


        mSetReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (searchText != null) {
                    fabOnClick();
                } else {
                    Toast.makeText(ReminderMapsActivity.this,"Please Enter a Location",Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void fabOnClick() {

            mLocationDatabase.child("text").setValue(mReminderText).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Intent intent = new Intent(ReminderMapsActivity.this,MainActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(),"Something went wrong!",Toast.LENGTH_SHORT).show();
                    }
                }
            });

    }


    //------GEO TAGGING CODES-----//

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (checkPlayServices()){
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();

                    }

                }
                break;
        }
    }

    private void setUpLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //Request Runtime permission
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);

        } else {
            if (checkPlayServices()){
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
                
            }
        }
    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation != null){
            final double latitude = mLastLocation.getLatitude();
            final double longitude = mLastLocation.getLongitude();

            //Update to firebase
            geofire.setLocation("You", new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    //Add marker
                    if(mCurrentMarker != null){
                        mCurrentMarker.remove(); //remove old marker

                    }
                    mCurrentMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude))
                                                        .title("You"));

                    //Move camera to this position
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),12.0f));
                }
            });



            Log.d("FnF",String.format("Your locatioon was changed: %f / %f", latitude, longitude));
        } else {
            Log.d("Fnf","Can not access your location");
        }

    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if(resultCode != ConnectionResult.SUCCESS){

            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(this, "Device not supported",Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        init();

//        mLocationDatabase.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot chatSnapshot: dataSnapshot.getChildren()) {
//                    String key = chatSnapshot.getKey();
//                    System.out.print("node name:" + key);
//                }
//                String lat = dataSnapshot.child("Reminder").child("l").child("0").getValue().toString();
//                String lng = dataSnapshot.child("Reminder").child("l").child("1").getValue().toString();
//
//
//                LatLng reminder_area = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));//retrieve the location lat and long
//
//
//                CircleOptions options = new CircleOptions()
//                        .center(reminder_area)
//                        .radius(500)        //radius in metres
//                        .strokeColor(Color.BLUE)
//                        .fillColor(0x220000FF)
//                        .strokeWidth(5.0f);
//
//                mapCircle = mMap.addCircle(options);
//
//
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void sendNotification(String title, String content) {
//
//
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        // create channel in new versions of android
//
//            int importance = NotificationManager.IMPORTANCE_LOW;
//            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
//            notificationChannel.enableLights(true);
//            notificationChannel.setLightColor(Color.RED);
//            notificationChannel.enableVibration(true);
//            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
//            notificationManager.createNotificationChannel(notificationChannel);
//
//
//
//        // show notification
//        Intent intent = new Intent(this, ReminderMapsActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        // 0 is request code
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
//
//        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder notificationBuilder =
//                new NotificationCompat.Builder(this, CHANNEL_ID)
//                        .setSmallIcon(R.drawable.ic_noti)
//                        .setContentTitle(title)
//                        .setContentText(content)
//                        .setAutoCancel(true)
//                        //.setSound(defaultSoundUri)
//                        .setContentIntent(pendingIntent);
//
//        // 0 is id of notification
//        notificationManager.notify(0, notificationBuilder.build());
//    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();

    }

    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();

    }

    //------------------------------Search Area------------------------//

    private void init(){
        Log.d("TAG","innit: initialzing");

        cGoogleApiClient= new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this,this)
                .build();

        mSearchText.setOnItemClickListener(mAutoCompleteClickListener);

        placeAutocompleteAdapter = new PlaceAutocompleteAdapter(this, cGoogleApiClient, LAT_LNG_BOUNDS,null);

        mSearchText.setAdapter(placeAutocompleteAdapter);

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_SEARCH
                        || i == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute method for searching
                    geoLocate();

                }

                return false;
            }
        });
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayLocation();
            }
        });

        hideSoftKeyboard();
    }

    private void geoLocate() {
        Log.d("TAG","geolocating:");

        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(ReminderMapsActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);

        }catch (IOException e){
            Log.d("TAG", "geolocate: IOException" + e.getMessage() );
        }

        if(list.size() > 0) {
            Address address = list.get(0);

            Log.d("TAG", "Geolocate address: " + address.toString() );


            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()));
            hideSoftKeyboard();

        }


    }

    private void moveCamera(final LatLng latLng){
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        final double latitude = latLng.latitude;
        final double longitude = latLng.longitude;


        if(mapCircle != null) {
            mapCircle.remove(); //remove old marker
        }

        geofire.setLocation("Reminder", new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        //Add marker
                        CircleOptions options = new CircleOptions()
                                .center(latLng)
                                .radius(500)        //radius in metres
                                .strokeColor(Color.BLUE)
                                .fillColor(0x220000FF)
                                .strokeWidth(5.0f);

                        mapCircle = mMap.addCircle(options);

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));

                        //Add geoQuery
                        //0.5f = 0.5km = 500m
//                        GeoQuery geoQuery = geofire.queryAtLocation(new GeoLocation(latitude,longitude),0.1f);
//                        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
//                            @RequiresApi(api = Build.VERSION_CODES.O)
//                            @Override
//                            public void onKeyEntered(String key, GeoLocation location) {
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////                                    sendNotification("Fnf",String.format("%s entered the dangerous area",key));
//                                    sendNotification("Fnf",String.format("%s: " + mReminderText,key));
//                                }
//
//                            }
//
//                            @RequiresApi(api = Build.VERSION_CODES.O)
//                            @Override
//                            public void onKeyExited(String key) {
//                                sendNotification("Fnf",String.format("%s left the dangerous area",key));
//
//                            }
//
//                            @Override
//                            public void onKeyMoved(String key, GeoLocation location) {
//                                Log.d("MOVE",String.format("%s moved within the area [%f/%f]",key,location.latitude,location.longitude));
//
//                            }
//
//                            @Override
//                            public void onGeoQueryReady() {
//
//                            }
//
//                            @Override
//                            public void onGeoQueryError(DatabaseError error) {
//                                Log.e("ERROR",""+error);
//
//                            }
//                        });
//
//
                    }
                });




    }
    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    //*************************AUTOCOMPLETE GOOGLE PLACES API -------------------------------------------------//

    private AdapterView.OnItemClickListener mAutoCompleteClickListener = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
            hideSoftKeyboard();

            final AutocompletePrediction item = placeAutocompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(cGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };
    //get the place object we are looking for
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                Log.d("FNF", "onResult: Places query did not complete: " + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = places.get(0);

            try{
                mPlace = new PlaceInfo();
                mPlace.setName(place.getName().toString());
                mPlace.setAddress(place.getAddress().toString());
//                mPlace.setAttributions(place.getAttributions().toString());
                mPlace.setId(place.getId());
                mPlace.setLatLng(place.getLatLng());
                mPlace.setRating(place.getRating());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                mPlace.setWebsiteUri(place.getWebsiteUri());

                Log.d("fnf","onResult: place " + mPlace.toString());


            }catch (NullPointerException e){
                Log.e("Tag", "onResult: NullPointerException: " + e.getMessage() );
            }
            moveCamera(new LatLng(place.getViewport().getCenter().latitude,place.getViewport().getCenter().longitude));


            places.release();
        }
    };


}



