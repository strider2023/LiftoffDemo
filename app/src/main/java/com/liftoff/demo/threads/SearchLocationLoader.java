package com.liftoff.demo.threads;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.liftoff.demo.dao.Location;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by arindamnath on 09/12/15.
 */
public class SearchLocationLoader extends AsyncTaskLoader<List<Location>> {

    private final String mKey = "key=AIzaSyCeJlNPeS3YVr4wHChGyhuD4AMtUSTDVXs";
    private InputStream iStream = null;
    private HttpURLConnection urlConnection = null;
    private String queryURL, reference, sensor, parameters, output, types, searchParam;

    public SearchLocationLoader(Context context, String searchParam) {
        super(context);
        this.searchParam = searchParam;
    }

    @Override
    public List<Location> loadInBackground() {
        try {
            List<Location> data = new ArrayList<>();
            for (final HashMap<String, String> hMap : getLocationSearchDetails()) {
                data.addAll(getLocationDetails(hMap.get("reference")));
            }
            return data;
        } catch (Exception e) {
            Log.d("Exception", e.toString());
            return null;
        }
    }

    private List<HashMap<String, String>> getLocationSearchDetails() throws Exception {
        searchParam = "input=" + URLEncoder.encode(searchParam, "utf-8");
        sensor = "sensor=false";
        types = "types=geocode";
        parameters = searchParam + "&" + types + "&" + sensor + "&" + mKey;
        output = "json";
        queryURL = "https://maps.googleapis.com/maps/api/place/autocomplete/" + output + "?" + parameters;

        URL url = new URL(queryURL);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.connect();
        iStream = urlConnection.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
        StringBuffer sb = new StringBuffer();
        String line = "";
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        String jsonData = sb.toString();
        Log.e("Test", jsonData);
        br.close();
        return placesParse(new JSONObject(jsonData));
    }

    private List<Location> getLocationDetails(String reference) throws Exception {
        reference = "reference=" + reference;
        sensor = "sensor=false";
        parameters = reference + "&" + sensor + "&" + mKey;
        output = "json";
        queryURL = "https://maps.googleapis.com/maps/api/place/details/" + output + "?" + parameters;

        URL url = new URL(queryURL);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.connect();
        iStream = urlConnection.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
        StringBuffer sb = new StringBuffer();
        String line = "";
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        String jsonData = sb.toString();
        Log.e("Test", jsonData);
        br.close();
        return placeDetailsParse(new JSONObject(jsonData));
    }

    public List<HashMap<String, String>> placesParse(JSONObject jObject) throws Exception {
        JSONArray jPlaces = jObject.getJSONArray("predictions");
        int placesCount = jPlaces.length();
        List<HashMap<String, String>> placesList = new ArrayList<>();
        for (int i = 0; i < placesCount; i++) {
            HashMap<String, String> place = new HashMap<>();
            JSONObject jPlace = (JSONObject) jPlaces.get(i);
            reference = jPlace.getString("reference");
            place.put("description", jPlace.getString("description"));
            place.put("_id", jPlace.getString("id"));
            place.put("reference", jPlace.getString("reference"));
            placesList.add(place);
        }
        return placesList;
    }

    public List<Location> placeDetailsParse(JSONObject jObject) throws Exception {
        List<Location> list = new ArrayList<>();
        Double lat = (Double) jObject.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").get("lat");
        Double lng = (Double) jObject.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").get("lng");
        String formattedAddress = (String) jObject.getJSONObject("result").get("formatted_address");
        list.add(new Location(lat.doubleValue(), lng.doubleValue(), formattedAddress));
        return list;
    }
}
