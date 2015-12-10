package com.liftoff.demo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.liftoff.demo.R;
import com.liftoff.demo.dao.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arindamnath on 09/12/15.
 */
public class LocationListAdapter extends BaseAdapter {

    private List<Location> data = new ArrayList<>();
    private LayoutInflater mInflater;

    public LocationListAdapter(Context context) {
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(List<Location> listData) {
        data = listData;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Location getItem(int position) {
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
            convertView = mInflater.inflate(R.layout.adapter_location, parent, false);
            holder.mHeaderText = (TextView) convertView.findViewById(R.id.location_list_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mHeaderText.setText(data.get(position).getAddress());
        holder.mHeaderText.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_locations, 0, 0, 0);
        return convertView;
    }

    static class ViewHolder {
        TextView mHeaderText;
    }
}

