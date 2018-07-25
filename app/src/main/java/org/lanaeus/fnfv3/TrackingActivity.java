package org.lanaeus.fnfv3;

import android.graphics.Bitmap;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private String userName,list_user_id;
    private Toolbar mToolbar;

    DatabaseReference mLocationDatabase, mUsersDatabase;

    Double lat, lng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mToolbar = (Toolbar) findViewById(R.id.map_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Locate");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mLocationDatabase = FirebaseDatabase.getInstance().getReference().child("Locations");

        if (getIntent() != null){
            userName = getIntent().getStringExtra("user_name");
            list_user_id = getIntent().getStringExtra("user_id");
            lat = getIntent().getDoubleExtra("lat",0);
            lng = getIntent().getDoubleExtra("lng",0);

        }
        if (!TextUtils.isEmpty(list_user_id)){
            loadLocation(list_user_id);

        }
    }

    private void loadLocation(String list_user_id) {
        Query user_location = mLocationDatabase.orderByKey().equalTo(list_user_id);

        user_location.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot:dataSnapshot.getChildren()){
                   Locations trackingActivity = postSnapShot.getValue(Locations.class);

                   LatLng friendLocation = new LatLng(Double.parseDouble(trackingActivity.getLat()),Double.parseDouble(trackingActivity.getLng()));

                   //Create user location
                    Location currUser = new Location("");
                    currUser.setLatitude(lat);
                    currUser.setLongitude(lng);

                    //Create friend Location
                    Location friend = new Location("");
                    friend.setLatitude(Double.parseDouble(trackingActivity.getLat()));
                    friend.setLongitude(Double.parseDouble(trackingActivity.getLng()));

                    //Distance


                    //Add Marker for friend
                    mMap.addMarker(new MarkerOptions()
                    .position(friendLocation).title(userName).snippet("Distance " + new DecimalFormat("#.#").format(distance(currUser, friend)))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),12.0f));


                    //Add Marker for Current User
                    LatLng current = new LatLng(lat,lng);
                    mMap.addMarker(new MarkerOptions().position(current).title(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    private double distance(Location currUser, Location friend) {
        double theta = currUser.getLongitude() - friend.getLongitude();
        double dist = Math.sin(deg2rad(currUser.getLatitude()))
                * Math.sin(deg2rad(friend.getLatitude()))
                * Math.cos(deg2rad(currUser.getLatitude()))
                * Math.cos(deg2rad(friend.getLatitude()))
                * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);

    }

    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


    }
}
