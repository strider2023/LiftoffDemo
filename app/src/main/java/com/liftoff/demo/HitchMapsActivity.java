package com.liftoff.demo;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
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
import com.liftoff.demo.adapters.NearbyListAdapter;
import com.liftoff.demo.dao.UserLocation;
import com.liftoff.demo.dao.interfaces.LocationDirectionRequestListener;
import com.liftoff.demo.dao.interfaces.LocationRequestListener;
import com.liftoff.demo.threads.LocationDirectionTask;
import com.liftoff.demo.threads.LocationTask;
import com.liftoff.demo.util.AppLocationListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HitchMapsActivity extends FragmentActivity
        implements OnMapReadyCallback, LocationRequestListener, LocationDirectionRequestListener {

    private static final int START = 1;
    private static final int END = 2;

    private NearbyListAdapter nearbyListAdapter;
    private GoogleMap mMap;
    private AppLocationListener appLocationListener;
    private ListView nearbyList;
    private TextView startAddress, destinationAddress, carsNearbyText, selectNearby;
    private MarkerOptions startMarker, endMarker;
    private boolean isStartLocationAvailable = false, isDestinationLocationAvailable = false;
    private Button startTrip;
    private Polyline carDirections, tripDirections;
    private List<UserLocation> userLocations = new ArrayList<>();
    private UserLocation selectedUserLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        startAddress = (TextView) findViewById(R.id.hitch_from_loaction_text);
        destinationAddress = (TextView) findViewById(R.id.hitch_to_loaction_text);
        carsNearbyText = (TextView) findViewById(R.id.hitch_cars_nearby_text);
        startTrip = (Button) findViewById(R.id.hitch_directions_button);
        nearbyList = (ListView) findViewById(R.id.hitch_nearby_list);
        selectNearby = (TextView) findViewById(R.id.hitch_request_ride_button);

        nearbyListAdapter = new NearbyListAdapter(this);
        appLocationListener = new AppLocationListener(this);
        startMarker = new MarkerOptions().draggable(true).title("Start")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_start));
        endMarker = new MarkerOptions().draggable(true).title("Destination")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_end));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.hitch_map);
        mapFragment.getMapAsync(this);

        findViewById(R.id.hitch_start_location_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation(END, -1, -1);
            }
        });

        findViewById(R.id.hitch_end_location_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Enable search if destination not selected
                if(!isDestinationLocationAvailable) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.putExtra("requestCode", END);
                    intent.setClass(HitchMapsActivity.this, SearchLocationActivity.class);
                    startActivityForResult(intent, END);
                }
            }
        });

        findViewById(R.id.hitch_cars_nearby_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isStartLocationAvailable && isDestinationLocationAvailable) {
                    showAll();
                    if (carDirections != null) {
                        carDirections.remove();
                    }
                    if (tripDirections != null) {
                        tripDirections.remove();
                    }
                    findViewById(R.id.hitch_bottom_nearby_holder).setVisibility(View.VISIBLE);
                    findViewById(R.id.hitch_bottom_button_bar).setVisibility(View.GONE);
                }
            }
        });

        startTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LocationDirectionTask(0, v.getContext(), HitchMapsActivity.this)
                        .execute(new LatLng[]{startMarker.getPosition(), selectedUserLocation.getCurrPosition()});
                new LocationDirectionTask(1, v.getContext(), HitchMapsActivity.this)
                        .execute(new LatLng[]{selectedUserLocation.getCurrPosition(), endMarker.getPosition()});
            }
        });

        nearbyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                nearbyList.setVisibility(View.GONE);
                findViewById(R.id.hitch_bottom_nearby_holder).setVisibility(View.GONE);
                findViewById(R.id.hitch_bottom_button_bar).setVisibility(View.VISIBLE);
                selectedUserLocation = userLocations.get(position);
                hideRest(selectedUserLocation);
            }
        });

        selectNearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nearbyList.setVisibility(View.VISIBLE);
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
                    findViewById(R.id.hitch_bottom_nearby_holder).setVisibility(View.VISIBLE);
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
                    findViewById(R.id.hitch_bottom_nearby_holder).setVisibility(View.VISIBLE);
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
                new LocationTask(id, this, HitchMapsActivity.this).execute(new Double[] {location.getLatitude(), location.getLongitude()});
            } else if (appLocationListener.getLocation(LocationManager.NETWORK_PROVIDER) != null) {
                location = appLocationListener.getLocation(LocationManager.NETWORK_PROVIDER);
                new LocationTask(id, this, HitchMapsActivity.this).execute(new Double[] {location.getLatitude(), location.getLongitude()});
            } else {
                Log.d(getClass().getName(), "N/A");
            }
        } else {
            new LocationTask(id, this, HitchMapsActivity.this).execute(new Double[]{lat, lng});
        }
    }

    @Override
    public void onSuccess(int id, double longitude, double latitude, String address, String country) {
        if(id == START) {
            startMarker.position(new LatLng(latitude, longitude));
            if (!isStartLocationAvailable) {
                isStartLocationAvailable = true;
                mMap.addMarker(startMarker);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), mMap.getMaxZoomLevel() - 7.0f));
                addStaticCars(latitude, longitude);
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
        if (directions.size() > 0) {
            if(id == 0) {
                if (carDirections != null) {
                    carDirections.remove();
                }
            } else if(id == 1){
                if (tripDirections != null) {
                    tripDirections.remove();
                }
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
                if(id == 0) {
                    lineOptions.color(Color.RED);
                } else if(id == 1) {
                    lineOptions.color(Color.BLUE);
                }
            }
            if(id == 0) {
                carDirections = mMap.addPolyline(lineOptions);
            } else if(id == 1) {
                tripDirections = mMap.addPolyline(lineOptions);
            }
        } else {
            Toast.makeText(this, "Failed to get directions.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFaliure() {
        Toast.makeText(this, "Failed to get directions.", Toast.LENGTH_SHORT).show();
    }

    /**
     * This function adds some static cars nearby for demo application.
     * However in a real application this needs to handled dynamically from a webservice using services.
     * @param latitude
     * @param longitude
     */
    private void addStaticCars(double latitude, double longitude) {
        userLocations.add(new UserLocation(1, "Arindam Nath", new LatLng(latitude - 0.0005d, longitude - 0.005d), "Kolkata"));
        userLocations.add(new UserLocation(2, "Random Nath", new LatLng(latitude + 0.005d, longitude - 0.002d), "Kolkata 2"));
        userLocations.add(new UserLocation(3, "Very Random Nath", new LatLng(latitude - 0.007d, longitude + 0.002d), "Kolkata 3"));

        carsNearbyText.setText(String.valueOf(userLocations.size()));
        nearbyListAdapter.setData(userLocations, latitude, longitude);
        nearbyList.setAdapter(nearbyListAdapter);
        for (UserLocation userLocation : userLocations) {
            userLocation.setCurrentPostion(mMap.addMarker(userLocation.getCurrentPositionMarker()));
        }
    }

    private void hideRest(UserLocation selectedId) {
        for (UserLocation userLocation : userLocations) {
            if(userLocation.getId() != selectedId.getId()) {
                userLocation.getCurrentPostion().setVisible(false);
            }
        }
    }

    private void showAll() {
        for (UserLocation userLocation : userLocations) {
            userLocation.getCurrentPostion().setVisible(true);
        }
    }
}
