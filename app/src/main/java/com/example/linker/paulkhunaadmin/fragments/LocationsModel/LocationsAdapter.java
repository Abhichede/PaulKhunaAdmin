package com.example.linker.paulkhunaadmin.fragments.LocationsModel;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.linker.paulkhunaadmin.R;

import java.util.List;

/**
 * Created by linker on 1/3/18.
 */

public class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.MyViewHolder> {
    private List<Location> moviesList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, desc, lat, lon;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            desc = (TextView) view.findViewById(R.id.description);
            lat = (TextView) view.findViewById(R.id.latitude);
            lon = (TextView) view.findViewById(R.id.longitude);
        }
    }


    public LocationsAdapter(List<Location> moviesList) {
        this.moviesList = moviesList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.location_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Location movie = moviesList.get(position);
        holder.title.setText(movie.getName());
        holder.desc.setText(movie.getDescription());
        holder.lat.setText(movie.getLatitude());
        holder.lon.setText(movie.getLongitude());
    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }
}
