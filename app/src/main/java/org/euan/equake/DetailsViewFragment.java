package org.euan.equake;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//Euan McDonald
//StudentID - s1927457
public class DetailsViewFragment extends Fragment implements OnMapReadyCallback {

    private TextView quakeTitle;
    private TextView quakeDescription;
    private TextView positionDetails;
    private MapView mapView;

    private EQuakeEntry SelectedEQuake;

    //Update the SelectedEQuake var withe the EQuake Data passed in
    public void updateDetailsData(EQuakeEntry EQuakeData) {
        this.SelectedEQuake  = EQuakeData;
    }

    public DetailsViewFragment()
    {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.details_view_fragment, container, false);
        //setContentView(R.layout.details_view_fragment);
        MainActivity activity = (MainActivity)getActivity();
        if (activity != null) {
            activity.getSupportActionBar().show();
            activity.getSupportActionBar().setTitle("EarthQuake Details");
            activity.showActionBarBack();
        }
        if (savedInstanceState != null) {
            SelectedEQuake = (EQuakeEntry) savedInstanceState.getSerializable("SelectedEQuake");
        }


        quakeTitle = (TextView) v.findViewById(R.id.title);
        quakeDescription = (TextView) v.findViewById(R.id.description);
        positionDetails = (TextView) v.findViewById(R.id.positionDetails);


        //SelectedEQuake = (EQuakeEntry) getIntent().getSerializableExtra("QuakeData");

        String EQDescription = SelectedEQuake.getDescription();
        String[] descriptionSections = EQDescription.split(" ; ");
        String[] locationParts = descriptionSections[1].split(": ");
        String[] depthParts = descriptionSections[3].split(": ");
        String[] magnatudeParts = descriptionSections[4].split(": ");
        String quakeDepth  = depthParts[1].replaceAll("\\s","");

        String Quakelocation = "";
        if(locationParts[1].contains(",")){
            String[] locationSections = locationParts[1].split(",");
            locationSections[0] = locationSections[0].toLowerCase();
            locationSections[0] = locationSections[0].substring(0, 1).toUpperCase() + locationSections[0].substring(1);
            locationSections[1] = locationSections[1].toLowerCase();
            locationSections[1] = locationSections[1].substring(0, 1).toUpperCase() + locationSections[1].substring(1);
            Quakelocation = locationSections[0] + " " + locationSections[1];
        }else{
            Quakelocation = locationParts[1].toLowerCase();
            Quakelocation = Quakelocation.substring(0, 1).toUpperCase() + Quakelocation.substring(1);
        }


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss");
        LocalDateTime quakeDateAndTime = LocalDateTime.parse(SelectedEQuake.getPubDate(), formatter);

        String quakeDay = quakeDateAndTime.getDayOfWeek().toString().toLowerCase();
        quakeDay = quakeDay.substring(0, 1).toUpperCase() + quakeDay.substring(1);
        String quakeMonth = quakeDateAndTime.getMonth().toString().toLowerCase();
        quakeMonth = quakeMonth.substring(0, 1).toUpperCase() + quakeMonth.substring(1);


        quakeTitle.setText("This EarthQuake was detected in the area of "+ Quakelocation + " at roughly " + quakeDateAndTime.toLocalTime() + " on " + quakeDay + " the " + quakeDateAndTime.getDayOfMonth()  + " of " + quakeMonth + " " + quakeDateAndTime.getYear());
        quakeDescription.setText("This EarthQuake had a Magnitude of " + magnatudeParts[1] +"ML");
        positionDetails.setText("The EarthQuake Was recorded to occur at the coordinates " + SelectedEQuake.getLatitude() + ", " + SelectedEQuake.getLongitude() + " at a depth of " + quakeDepth + ", The location can be viewed below.");

        setHasOptionsMenu(true);
        return v;
    }

    //Handle the click of the Activity's back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ((MainActivity)getActivity()).onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Create the Google map to be displayed
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) view.findViewById(R.id.detailsMap);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    //Populate the map with a Marker once it is created
    @Override
    public void onMapReady(GoogleMap googleMap) {
        String EQDescription = SelectedEQuake.getDescription();
        String[] descriptionSections = EQDescription.split(" ; ");
        String[] magnatudeParts = descriptionSections[4].split(": ");

        LatLng marker = new LatLng(SelectedEQuake.getLatitude(), SelectedEQuake.getLongitude());
        googleMap.addMarker(new MarkerOptions()
                .position(marker)
                .title(descriptionSections[1])
                .snippet(descriptionSections[4]));


        LatLngBounds uKBounds = new LatLngBounds(
                new LatLng(49, -9.00), // SW bounds
                new LatLng(61, 0.868)  // NE bounds
        );
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(uKBounds, 1, 1, 0));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker, 12));
    }

    //Save the Current Values to the bundle when device rotates
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("SelectedEQuake", (Serializable) SelectedEQuake);
    }
}
