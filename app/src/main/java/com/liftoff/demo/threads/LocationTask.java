package com.liftoff.demo.threads;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;

import com.liftoff.demo.dao.enums.Event;
import com.liftoff.demo.dao.interfaces.LocationRequestListener;
import com.liftoff.demo.util.NetworkUtil;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by arindamnath on 08/12/15.
 */
public class LocationTask extends AsyncTask<Double, Void, Event> {

    private int id;
    private Geocoder geocoder;
    private String address;
    private String country;
    private LocationRequestListener locationRequestListener;
    private double longitude, latitude;
    private NetworkUtil networkUtil;

    public LocationTask(int id, Context context, LocationRequestListener locationRequestListener) {
        this.id = id;
        this.geocoder = new Geocoder(context, Locale.getDefault());
        this.locationRequestListener = locationRequestListener;
        networkUtil = new NetworkUtil(context);
    }

    @Override
    protected Event doInBackground(Double... params) {
        if(networkUtil.isNetworkAvailable()) {
            try {
                latitude = params[0];
                longitude = params[1];
                List<Address> addressList = geocoder.getFromLocation(
                        latitude, longitude, 1);
                if (addressList != null && addressList.size() > 0) {
                    Address address = addressList.get(0);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        sb.append(address.getAddressLine(i));
                    }
                    this.address = sb.toString();
                    this.country = address.getCountryName();
                }
            } catch (IOException e) {
                return Event.NO_NETWORK;
            } finally {
                if (this.address != null) {
                    return Event.SUCCESS;
                } else {
                    return Event.FALIURE;
                }
            }
        } else {
            return Event.NO_NETWORK;
        }
    }

    @Override
    protected void onPostExecute(Event serverEvent) {
        super.onPostExecute(serverEvent);
        switch (serverEvent) {
            case SUCCESS:
                locationRequestListener.onSuccess(id, longitude, latitude, address, country);
                break;
            case FALIURE:
                locationRequestListener.onFailure(id);
                break;
        }
    }
}

