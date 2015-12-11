package com.liftoff.demo.threads;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.liftoff.demo.dao.enums.Event;
import com.liftoff.demo.dao.interfaces.LocationDirectionRequestListener;
import com.liftoff.demo.util.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by arindamnath on 10/12/15.
 * http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
 */
public class LocationDirectionTask extends AsyncTask<LatLng, Void, Event> {

    private int requestId;
    private ProgressDialog progressDialog;
    private LocationDirectionRequestListener locationDirectionRequestListener;
    private List<List<HashMap<String, String>>> routes = null;
    private NetworkUtil networkUtil;

    public LocationDirectionTask(int requestId, Context context, LocationDirectionRequestListener locationDirectionRequestListener) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching directions...");
        this.locationDirectionRequestListener = locationDirectionRequestListener;
        this.requestId = requestId;
        this.networkUtil = new NetworkUtil(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.show();
    }

    @Override
    protected Event doInBackground(LatLng... params) {
        if(networkUtil.isNetworkAvailable()) {
            try {
                String str_origin = "origin=" + params[0].latitude + "," + params[0].longitude;
                String str_dest = "destination=" + params[1].latitude + "," + params[1].longitude;
                String sensor = "sensor=false";
                String parameters = str_origin + "&" + str_dest + "&" + sensor;
                String output = "json";
                String strUrl = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

                String data = "";
                InputStream iStream = null;
                HttpURLConnection urlConnection = null;

                URL url = new URL(strUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                iStream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
                StringBuffer sb = new StringBuffer();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                data = sb.toString();
                br.close();
                Log.e("Test", data);
                routes = parse(new JSONObject(data));
                return Event.SUCCESS;
            } catch (Exception e) {
                return Event.EXCEPTION;
            }
        } else {
            return Event.NO_NETWORK;
        }
    }

    @Override
    protected void onPostExecute(Event event) {
        super.onPostExecute(event);
        progressDialog.dismiss();
        switch (event) {
            case SUCCESS:
                locationDirectionRequestListener.onSuccess(requestId, routes);
                break;
            case EXCEPTION:
                locationDirectionRequestListener.onFaliure();
                break;
        }
    }

    private List<List<HashMap<String, String>>> parse(JSONObject jObject) throws Exception {
        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;
        jRoutes = jObject.getJSONArray("routes");
        /** Traversing all routes */
        for (int i = 0; i < jRoutes.length(); i++) {
            jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
            List path = new ArrayList();
            /** Traversing all legs */
            for (int j = 0; j < jLegs.length(); j++) {
                jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                /** Traversing all steps */
                for (int k = 0; k < jSteps.length(); k++) {
                    String polyline = "";
                    polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                    List<LatLng> list = decodePoly(polyline);
                    /** Traversing all points */
                    for (int l = 0; l < list.size(); l++) {
                        HashMap<String, String> hm = new HashMap<String, String>();
                        hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                        hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
                        path.add(hm);
                    }
                }
                routes.add(path);
            }
        }
        return routes;
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
}
