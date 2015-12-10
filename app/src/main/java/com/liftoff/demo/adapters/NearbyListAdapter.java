package com.liftoff.demo.adapters;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.liftoff.demo.R;
import com.liftoff.demo.dao.UserLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arindamnath on 10/12/15.
 */
public class NearbyListAdapter extends BaseAdapter {

    private List<UserLocation> data = new ArrayList<>();
    private LayoutInflater mInflater;
    private double userLat, userLong;

    public NearbyListAdapter(Context context) {
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(List<UserLocation> listData, double userLat, double userLong) {
        data = listData;
        this.userLat = userLat;
        this.userLong = userLong;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public UserLocation getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.adapter_cars_request, parent, false);
            holder.mHeaderText = (TextView) convertView.findViewById(R.id.request_user_text);
            holder.mSubtitleText = (TextView) convertView.findViewById(R.id.request_destination_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Location selected_location=new Location("locationA");
        selected_location.setLatitude(data.get(position).getCurrPosition().latitude);
        selected_location.setLongitude(data.get(position).getCurrPosition().longitude);

        Location near_locations=new Location("locationA");
        near_locations.setLatitude(userLat);
        near_locations.setLongitude(userLong);

        holder.mHeaderText.setText(data.get(position).getUserName());
        holder.mSubtitleText.setText(data.get(position).getDesitnationAddress() + " | " +
                String.valueOf(selected_location.distanceTo(near_locations)) + " meters form you");
        return convertView;
    }

    static class ViewHolder {
        TextView mHeaderText, mSubtitleText;
    }
}