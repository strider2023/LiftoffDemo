package com.liftoff.demo.dao;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.liftoff.demo.R;

/**
 * Created by arindamnath on 10/12/15.
 * Lots of additional user information can be crunched here. Like phone number, available seats etc.
 */
public class UserLocation {

    private int id;
    private String userName;
    private LatLng currPosition;
    private LatLng originPosition;
    private LatLng destinationPosition;
    private String desitnationAddress;
    private MarkerOptions currentPositionMarker;
    private Marker currentPostion;

    public UserLocation(int id, String userName, LatLng currPosition, String desitnationAddress) {
        this.id = id;
        this.userName = userName;
        this.currPosition = currPosition;
        this.desitnationAddress = desitnationAddress;
        this.currentPositionMarker = new MarkerOptions().draggable(false)
                .title(userName)
                .snippet(desitnationAddress)
                .position(currPosition)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car));
    }

    public int getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public LatLng getCurrPosition() {
        return currPosition;
    }

    public LatLng getOriginPosition() {
        return originPosition;
    }

    public LatLng getDestinationPosition() {
        return destinationPosition;
    }

    public String getDesitnationAddress() {
        return desitnationAddress;
    }

    public MarkerOptions getCurrentPositionMarker() {
        return currentPositionMarker;
    }

    public Marker getCurrentPostion() {
        return currentPostion;
    }

    public void setCurrentPostion(Marker currentPostion) {
        this.currentPostion = currentPostion;
    }
}
