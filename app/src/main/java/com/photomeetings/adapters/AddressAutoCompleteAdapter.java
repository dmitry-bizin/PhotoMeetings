package com.photomeetings.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.photomeetings.R;
import com.photomeetings.model.Point;
import com.photomeetings.services.GeoService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddressAutoCompleteAdapter extends BaseAdapter implements Filterable {

    private final Context context;
    private List<Point> result;

    public AddressAutoCompleteAdapter(Context context) {
        this.context = context;
        result = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return result.size();
    }

    @Override
    public Point getItem(int index) {
        return result.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.find_address_layout, parent, false);
        }
        Point point = getItem(position);
        ((TextView) convertView.findViewById(R.id.addressTextView)).setText(point.getAddress());
        return convertView;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    List<Point> points = findPoints(constraint.toString());
                    filterResults.values = points;
                    filterResults.count = points.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    result = (List<Point>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    private List<Point> findPoints(String address) {
        List<Point> points = new ArrayList<>();
        try {
            points.addAll(GeoService.geocoding(address));
        } catch (IOException e) {
            Toast.makeText(context, R.string.geo_network_error, Toast.LENGTH_LONG).show();
        }
        return points;
    }

}
