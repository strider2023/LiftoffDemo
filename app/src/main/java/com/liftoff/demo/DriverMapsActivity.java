package com.liftoff.demo;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.liftoff.demo.dao.interfaces.LocationDirectionRequestListener;
import com.liftoff.demo.dao.interfaces.LocationRequestListener;
import com.liftoff.demo.threads.LocationDirectionTask;
import com.liftoff.demo.threads.LocationTask;
import com.liftoff.demo.util.AppLocationListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by arindamnath on 08/12/15.
 */
public class DriverMapsActivity extends FragmentActivity
        implements OnMapReadyCallback, LocationRequestListener, LocationDirectionRequestListener {

    private static final int START = 1;
    private static final int END = 2;

    private GoogleMap mMap;
    private AppLocationListener appLocationListener;
    private TextView startAddress, destinationAddress;
    private MarkerOptions startMarker, endMarker;
    private boolean isStartLocationAvailable = false, isDestinationLocationAvailable = false;
    private Button startTrip, navigationStart;
    private Polyline tripDirections;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);

        startAddress = (TextView) findViewById(R.id.driver_from_loaction_text);
        destinationAddress = (TextView) findViewById(R.id.driver_to_loaction_text);
        startTrip = (Button) findViewById(R.id.drive_directions_button);
        navigationStart = (Button) findViewById(R.id.drive_open_nav_button);

        appLocationListener = new AppLocationListener(this);
        startMarker = new MarkerOptions().draggable(true).title("Start")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_start));
        endMarker = new MarkerOptions().draggable(true).title("Destination")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_end));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.driver_map);
        mapFragment.getMapAsync(this);

        findViewById(R.id.drive_start_location_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation(END, -1, -1);
            }
        });

        findViewById(R.id.drive_end_location_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Enable search if destination not selected
                if(!isDestinationLocationAvailable) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.putExtra("requestCode", END);
                    intent.setClass(DriverMapsActivity.this, SearchLocationActivity.class);
                    startActivityForResult(intent, END);
                }
            }
        });

        startTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LocationDirectionTask(0, v.getContext(), DriverMapsActivity.this)
                        .execute(new LatLng[]{startMarker.getPosition(), endMarker.getPosition()});
            }
        });

        navigationStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" +
                        endMarker.getPosition().latitude + "," + endMarker.getPosition().longitude));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == END) {
            if (data != null) {
                if (!isDestinationLocationAvailable) {
                    endMarker.position(new LatLng(data.getDoubleExtra("lat", 0.0d), data.getDoubleExtra("lng", 0.0d)));
                    destinationAddress.setText(data.getStringExtra("address"));
                    mMap.addMarker(endMarker);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(endMarker.getPosition()));
                    isDestinationLocationAvailable = true;
                    findViewById(R.id.drive_bottom_button_bar).setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (marker.getTitle().equalsIgnoreCase("Start")) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                    getLocation(START, marker.getPosition().latitude, marker.getPosition().longitude);
                } else if (marker.getTitle().equalsIgnoreCase("Destination")) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                    getLocation(END, marker.getPosition().latitude, marker.getPosition().longitude);
                }
            }

            @Override
            public void onMarkerDragStart(Marker marker) {
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                if (!isDestinationLocationAvailable) {
                    endMarker.position(point);
                    getLocation(END, point.latitude, point.longitude);
                    mMap.addMarker(endMarker);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(endMarker.getPosition()));
                    isDestinationLocationAvailable = true;
                    findViewById(R.id.drive_bottom_button_bar).setVisibility(View.VISIBLE);
                }
            }
        });

        //Auto fetch current location
        getLocation(START, -1, -1);
    }

    private void getLocation(int id, double lat, double lng) {
        if(lat == -1 && lng == -1) {
            Location location;
            if (appLocationListener.getLocation(LocationManager.GPS_PROVIDER) != null) {
                location = appLocationListener.getLocation(LocationManager.GPS_PROVIDER);
                new LocationTask(id, this, DriverMapsActivity.this).execute(new Double[] {location.getLatitude(), location.getLongitude()});
            } else if (appLocationListener.getLocation(LocationManager.NETWORK_PROVIDER) != null) {
                location = appLocationListener.getLocation(LocationManager.NETWORK_PROVIDER);
                new LocationTask(id, this, DriverMapsActivity.this).execute(new Double[] {location.getLatitude(), location.getLongitude()});
            } else {
                Log.d(getClass().getName(), "N/A");
            }
        } else {
            new LocationTask(id, this, DriverMapsActivity.this).execute(new Double[] {lat, lng});
        }
    }

    @Override
    public void onSuccess(int id, double longitude, double latitude, String address, String country) {
        if(id == START) {
            startMarker.position(new LatLng(latitude, longitude));
            if (!isStartLocationAvailable) {
                isStartLocationAvailable = true;
                mMap.addMarker(startMarker);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), mMap.getMaxZoomLevel() - 10.0f));
            }
            startAddress.setText(address);
        } else if(id == END) {
            endMarker.position(new LatLng(latitude, longitude));
            destinationAddress.setText(address);
        }
    }

    @Override
    public void onFailure(int id) {
        Toast.makeText(this, "Failed to fetch current location!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccess(int id, List<List<HashMap<String, String>>> directions) {
        if(id == 0) {
            if (directions.size() > 0) {
                if (tripDirections != null) {
                    tripDirections.remove();
                }
                List<LatLng> points = new ArrayList<>();
                PolylineOptions lineOptions = null;
                for (int i = 0; i < directions.size(); i++) {
                    points = new ArrayList<>();
                    lineOptions = new PolylineOptions();
                    List<HashMap<String, String>> path = directions.get(i);
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);
                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);
                        points.add(position);
                    }

                    lineOptions.addAll(points);
                    lineOptions.width(5);
                    lineOptions.color(Color.BLUE);
                }
                tripDirections = mMap.addPolyline(lineOptions);
            } else {
                Toast.makeText(this, "Failed to get directions.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onFaliure() {
        Toast.makeText(this, "Failed to get directions.", Toast.LENGTH_SHORT).show();
    }
}
