package org.euan.equake;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//Euan McDonald
//StudentID - s1927457
public class MapViewFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private List<EQuakeEntry> EQuakeData = new ArrayList<>();

    //Update the list of EQuake with the data passed in
    public void updateMapData(List<EQuakeEntry> EQuakeData) {
        this.EQuakeData  = EQuakeData;
    }

    
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.map_view_fragment, container, false);
        MainActivity activity = (MainActivity)getActivity();
        if (activity != null) {
            activity.getSupportActionBar().hide();
            activity.hideActionBarBack();
        }
        if (savedInstanceState != null) {
            EQuakeData = (List<EQuakeEntry>) savedInstanceState.getSerializable("MapFragmentsEquakeData");
        }
        return v;
    }

    //Create the Google map to be displayed
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

    }

    //Populate the map with Markers showing the EQuakes
    @Override
    public void onMapReady(GoogleMap googleMap) {

        double maxMagnatude = 0;
        for(EQuakeEntry coord : EQuakeData) {
            String EQDescription = coord.getDescription();
            String[] descriptionSections = EQDescription.split(" ; ");
            String[] magnatudeParts = descriptionSections[4].split(": ");
            if (Double.parseDouble(magnatudeParts[1]) > maxMagnatude){
                maxMagnatude = Double.parseDouble(magnatudeParts[1]);
            }

        }

        //Colour code the markers by spiting them up into quartile groups based of the highest recoded magnitude
        for(EQuakeEntry coord : EQuakeData) {
            String EQDescription = coord.getDescription();
            String[] descriptionSections = EQDescription.split(" ; ");
            String[] magnatudeParts = descriptionSections[4].split(": ");

            float MarkerColour = (float) 270.0;
            if (Double.parseDouble(magnatudeParts[1])/maxMagnatude <= 0.25){
                MarkerColour = (float) 120.0;
            }
            else if(Double.parseDouble(magnatudeParts[1])/maxMagnatude > 0.25 && Double.parseDouble(magnatudeParts[1])/maxMagnatude <= 0.5){
                MarkerColour = (float) 60.0;
            }
            else if(Double.parseDouble(magnatudeParts[1])/maxMagnatude > 0.5 && Double.parseDouble(magnatudeParts[1])/maxMagnatude <= 0.75){
                MarkerColour = (float) 30.0;
            }
            else if(Double.parseDouble(magnatudeParts[1])/maxMagnatude > 0.75){
                MarkerColour = (float) 0.0;
            }
            LatLng marker = new LatLng(coord.getLatitude(), coord.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(marker)
                    .icon(BitmapDescriptorFactory.defaultMarker(MarkerColour))
                    .title(descriptionSections[1])
                    .snippet(descriptionSections[4]));
        }

        LatLngBounds uKBounds = new LatLngBounds(
                new LatLng(49, -12.00), // SW bounds
                new LatLng(61, 3.868)  // NE bounds
        );
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(uKBounds, 0));
    }

    //Save the Current Values to the bundle when device rotates
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("MapFragmentsEquakeData", (Serializable) EQuakeData);
    }
}
