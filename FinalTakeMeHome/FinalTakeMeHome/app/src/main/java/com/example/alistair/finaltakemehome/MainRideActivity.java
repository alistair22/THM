package com.example.alistair.finaltakemehome;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.Manifest;

public class MainRideActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, RoutingListener {

    TextView txtName, txtEmail, txtClass1,txtClass2;
    Button btnDestination, btnPickUp;
    FirebaseAuth auth;
    FirebaseUser user;
    String user_id;
    DatabaseReference reference;
    private GoogleMap mMap;
    GoogleApiClient client;
    LocationRequest request;
    LatLng startLatLng, endLatLng;
    Marker currentMarker;
    Marker destinationMarker;
    Location mLastLocation;
    FloatingActionButton fab,fab2;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ride);

        //location permission
        client = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();


        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                Toast.makeText(MainRideActivity.this, "The permission to get location data is required", Toast.LENGTH_SHORT).show();
            }else{requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
        }else {
            //Toast.makeText(MainRideActivity.this, "Location permissions already granted", Toast.LENGTH_SHORT).show();
        }
       /** if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                    PERMISSION_ACCESS_COARSE_LOCATION);
        }
**/
        auth = FirebaseAuth.getInstance();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar1);

        btnPickUp = (Button) findViewById(R.id.btnPickup);
        btnDestination = (Button) findViewById(R.id.btnDestination);
        txtClass1 = (TextView) findViewById(R.id.txtClass1);
        txtClass2 = (TextView) findViewById(R.id.txtClass2);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);

        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBusinessDialog();
            }
        });
       /** FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEconomyDialog();
            }
        });
        **/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        user = FirebaseAuth.getInstance().getCurrentUser();
        //not logged in
        if (user == null) {
            Intent intent = new Intent(MainRideActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            user_id = user.getUid();
            //points to parent-node in Firebase
            reference = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);

                    //initialise
                    txtName = (TextView) findViewById(R.id.txtName);
                    txtEmail = (TextView) findViewById(R.id.txtEmail);
                    //set text to Nav drawer header
                    txtName.setText(name);
                    txtEmail.setText(email);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        btnPickUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    Intent i = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(MainRideActivity.this);
                    startActivityForResult(i,200);

                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        btnDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    Intent i = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(MainRideActivity.this);
                    startActivityForResult(i,400);

                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void showBusinessDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Business Price Estimate");

        LayoutInflater inflater = LayoutInflater.from(this);
        View BusinessFare_layout = inflater.inflate(R.layout.business_price, null);

        TextView txt1 = (TextView) BusinessFare_layout.findViewById(R.id.txt1);
        TextView txt2 = (TextView) BusinessFare_layout.findViewById(R.id.txt2);
        TextView txtPrice = (TextView) BusinessFare_layout.findViewById(R.id.txtPrice);
        TextView txtEst = (TextView) BusinessFare_layout.findViewById(R.id.txtRands);
        TextView txtDesc1 = (TextView) BusinessFare_layout.findViewById(R.id.desc1);
        TextView txtDesc2 = (TextView) BusinessFare_layout.findViewById(R.id.desc2);

        dialog.setView(BusinessFare_layout);

        dialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.show();
    }

    private void showEconomyDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Economy Price Estimate");

        LayoutInflater inflater = LayoutInflater.from(this);
        View economyFare_layout = inflater.inflate(R.layout.econo_price, null);

        TextView txt1 = (TextView) economyFare_layout.findViewById(R.id.txt1);
        TextView txt2 = (TextView) economyFare_layout.findViewById(R.id.txt2);
        TextView txtPrice = (TextView) economyFare_layout.findViewById(R.id.txtPrice);
        TextView txtEst = (TextView) economyFare_layout.findViewById(R.id.txtRands);
        TextView txtDesc1 = (TextView) economyFare_layout.findViewById(R.id.desc1);
        TextView txtDesc2 = (TextView) economyFare_layout.findViewById(R.id.desc2);

        dialog.setView(economyFare_layout);

        dialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
           dialogInterface.dismiss();
            }
        });
dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (client != null) {
            client.connect();
        }
    }

    @Override
    protected void onStop() {
        client.disconnect();
        super.onStop();
    }

/**    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                } else {
                    Toast.makeText(this, "Need your location!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
**/
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        request = new LocationRequest().create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(1000);
        request.setFastestInterval(1000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
       // LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        if (location == null)
        {
            Toast.makeText(MainRideActivity.this, "Location Not Found", Toast.LENGTH_SHORT).show();
        }
        else {
            startLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            Geocoder geoCoder = new Geocoder(this, Locale.getDefault());

            try {
                List<android.location.Address> myaddresses = geoCoder.getFromLocation(startLatLng.latitude, startLatLng.longitude, 1);
                String address = myaddresses.get(0).getAddressLine(0);
                String city = myaddresses.get(0).getLocality();
                btnPickUp.setText(address + " "+city);
            }catch (IOException e)
            {
                e.printStackTrace();
            }

            if (currentMarker==null){
                MarkerOptions options = new MarkerOptions();
                options.position(startLatLng);
                options.title("Current Location");
                currentMarker = mMap.addMarker(options);

                mMap.moveCamera(CameraUpdateFactory.newLatLng(startLatLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
            }else {
                currentMarker.setPosition(startLatLng);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        client.connect();

        // Add a marker in Sydney and move the camera



    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_ride, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_ride) {


        } else if (id == R.id.nav_rideHistory) {
            Intent intent = new Intent(MainRideActivity.this, HistoryActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_notifications) {
            Intent intent = new Intent(MainRideActivity.this, NotificationActivity.class);
            startActivity(intent);
            return true;

        } else if (id == R.id.nav_rewards) {
            Intent intent = new Intent(MainRideActivity.this, RewardsActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_editProfile) {
            Intent intent = new Intent(MainRideActivity.this, ProfileActivity.class);
            startActivity(intent);


        } else if (id == R.id.nav_logout) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user!=null){
                auth.signOut();
                Intent intent = new Intent (MainRideActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }else {
                Toast.makeText(MainRideActivity.this, "User is already logged out!!", Toast.LENGTH_SHORT).show();
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == 200){
        if (resultCode == RESULT_OK){
            Place place = PlaceAutocomplete.getPlace(this,data);
            String name = place.getName().toString();
            startLatLng = place.getLatLng();
            btnPickUp.setText(name);

            if(currentMarker == null){
                MarkerOptions options1 = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_add_location_black_24dp));
                options1.title("Pickup Location");
                options1.position(startLatLng);
                currentMarker = mMap.addMarker(options1);
        }else {
                currentMarker.setPosition(startLatLng);
            }
        }
    }
        else if (requestCode == 400){
        if (resultCode == RESULT_OK){
            Place myplace = PlaceAutocomplete.getPlace(this, data);
            String name = myplace.getName().toString();
            endLatLng = myplace.getLatLng();
            btnDestination.setText(name);

            if (destinationMarker == null){
                //we need to put the marker
                MarkerOptions options1 = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_motorcycle_black_24dp));
                options1.title("Destination");
                options1.position(endLatLng);
                destinationMarker = mMap.addMarker(options1);

            }
            else {
                destinationMarker.setPosition(endLatLng);
            }
        }
    }
    }

    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> arrayList, int i) {

    }

    @Override
    public void onRoutingCancelled() {

    }
}
