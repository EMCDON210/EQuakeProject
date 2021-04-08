package org.euan.equake;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

//Euan McDonald
//StudentID - s1927457
public class MainFragment extends Fragment {


    private Button startButton;
    private Button nextPageButton;
    private Button searchButton;
    private Button clearButton;
    public static TextView firstDateView;
    public static TextView secondDateView;
    private LocalDate firstDate;
    private LocalDate secondDate;
    private String result = "";
    private String urlSource = "http://quakes.bgs.ac.uk/feeds/MhSeismology.xml";
    private List<EQuakeEntry> EQuakeData = new ArrayList<>();
    private List<EQuakeEntry> EQuakeDataInTimeOrdered = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView filterRecyclerView;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.main_fragment, container, false);
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.getSupportActionBar().hide();
            activity.hideActionBarBack();
        }

        startButton = (Button) v.findViewById(R.id.startButton);
        startButton.setOnClickListener(this::onClickLoadData);
        firstDateView = (TextView) v.findViewById(R.id.firstDate);
        firstDateView.setOnClickListener(this::showFirstDatePickerDialog);
        secondDateView = (TextView) v.findViewById(R.id.secondDate);
        secondDateView.setOnClickListener(this::showSecondDatePickerDialog);
        searchButton = (Button) v.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(this::onClickSearch);
        clearButton = (Button) v.findViewById(R.id.clearButton);
        clearButton.setOnClickListener(this::onClickClear);


        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        filterRecyclerView = (RecyclerView) v.findViewById(R.id.filterRecyclerView);


        GridLayoutManager gridLayoutManager;
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridLayoutManager = new GridLayoutManager(recyclerView.getContext(), 1);
            recyclerView.setLayoutManager(gridLayoutManager);
        } else {
            gridLayoutManager = new GridLayoutManager(recyclerView.getContext(), 2);
            recyclerView.setLayoutManager(gridLayoutManager);
        }
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), gridLayoutManager.getOrientation());
        recyclerView.addItemDecoration(mDividerItemDecoration);


        GridLayoutManager filtereGridLayoutManager;
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            filtereGridLayoutManager = new GridLayoutManager(filterRecyclerView.getContext(), 1);
            filterRecyclerView.setLayoutManager(filtereGridLayoutManager);
        } else {
            filtereGridLayoutManager = new GridLayoutManager(filterRecyclerView.getContext(), 2);
            filterRecyclerView.setLayoutManager(filtereGridLayoutManager);
        }
        DividerItemDecoration DividerItemDecoration = new DividerItemDecoration(filterRecyclerView.getContext(), filtereGridLayoutManager.getOrientation());
        filterRecyclerView.addItemDecoration(DividerItemDecoration);

        //load in any InstanceState data
        EQuakeDataInTimeOrdered.clear();
        if (savedInstanceState != null) {

            firstDate = (LocalDate) savedInstanceState.getSerializable("firstDate");
            secondDate = (LocalDate) savedInstanceState.getSerializable("secondDate");
            DateTimeFormatter formatterForEnteredDate = DateTimeFormatter.ofPattern("d/M/yyyy");
            List<EQuakeEntry> EQuakeDataSaved = (List<EQuakeEntry>) savedInstanceState.getSerializable("MainFragmentsEquakeData");
            List<EQuakeEntry> FilteredEQuakeDataSaved = (List<EQuakeEntry>) savedInstanceState.getSerializable("MainFragmentsFilteredEQuakeData");


            if (EQuakeDataSaved != null){
                EQuakeData = (List<EQuakeEntry>) savedInstanceState.getSerializable("MainFragmentsEquakeData");
            }
            if (FilteredEQuakeDataSaved != null){
                EQuakeDataInTimeOrdered = (List<EQuakeEntry>) savedInstanceState.getSerializable("MainFragmentsFilteredEQuakeData");
            }
            if (firstDate != null){
                firstDateView.setText(firstDate.format(formatterForEnteredDate) + "\n" + "(Click To Clear)");
            }
            if (secondDate != null){
                secondDateView.setText(secondDate.format(formatterForEnteredDate) + "\n" + "(Click To Clear)");
            }
        }

        //Display any filtered data f there is any
        if (!EQuakeDataInTimeOrdered.isEmpty()){
            CustomAdapterForFilter adapter = new CustomAdapterForFilter(EQuakeDataInTimeOrdered);
            filterRecyclerView.setAdapter(adapter);
            recyclerView.setVisibility(View.GONE);
            filterRecyclerView.setVisibility(View.VISIBLE);
            searchButton.setVisibility(View.GONE);
            clearButton.setVisibility(View.VISIBLE);
        }else{
            populateRecyclerView();
        }

        return v;
    }


    //Receive the current data to be stored for EQuakes
    public void ReceiveQuakeData(List<EQuakeEntry> EQuakeData) {
        this.EQuakeData = EQuakeData;
    }

    //Update the current data stored for EQuakes
    public void updateQuakeData(List<EQuakeEntry> EQuakeData) {
        this.EQuakeData.clear();
        this.EQuakeData = EQuakeData;
        CustomAdapter adapter = new CustomAdapter(EQuakeData);
        recyclerView.setAdapter(adapter);
    }

    public void onClickClear(View aview) {
        populateRecyclerView();
        EQuakeDataInTimeOrdered.clear();
        clearButton.setVisibility(View.GONE);
        searchButton.setVisibility(View.VISIBLE);
        filterRecyclerView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        firstDateView.setText("Click To Enter A Search Date");
        secondDateView.setText("- To The Date (Optional)");
        firstDate = null;
        secondDate = null;
    }

    //Display the datePicker for the user to select the First Date
    public void showFirstDatePickerDialog(View v) {
        if (firstDateView.getText().equals("Click To Enter A Search Date")) {
            DatePickerFragment newFragment = new DatePickerFragment();
            newFragment.show(getFragmentManager(), "firstDatePicker");
        } else {
            populateRecyclerView();
            clearButton.setVisibility(View.GONE);
            searchButton.setVisibility(View.VISIBLE);
            filterRecyclerView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            firstDateView.setText("Click To Enter A Search Date");
            secondDateView.setText("- To The Date (Optional)");
            EQuakeDataInTimeOrdered.clear();
            firstDate = null;
            secondDate = null;
        }
    }

    //Display the datePicker for the user to select the Second Date
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void showSecondDatePickerDialog(View v) {
        if (secondDateView.getText().equals("- To The Date (Optional)") && firstDateView.getText().equals("Click To Enter A Search Date")) {
            Toast.makeText(getActivity().getApplicationContext(), "Please First Enter A Start Date", Toast.LENGTH_SHORT).show();
        } else if (secondDateView.getText().equals("- To The Date (Optional)")) {
            DatePickerFragment newFragment = DatePickerFragment.newInstance(firstDate);
            newFragment.show(getFragmentManager(), "secondDatePicker");
        } else {
            populateRecyclerView();
            clearButton.setVisibility(View.GONE);
            searchButton.setVisibility(View.VISIBLE);
            filterRecyclerView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            secondDateView.setText("- To The Date (Optional)");
            EQuakeDataInTimeOrdered.clear();
            secondDate = null;
        }

    }

    //Update the firstDate var with the date entered
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateFirstDate(LocalDate firstDate) {
        this.firstDate = firstDate;
        DateTimeFormatter formatterForEnteredDate = DateTimeFormatter.ofPattern("d/M/yyyy");
        firstDateView.setText(firstDate.format(formatterForEnteredDate) + "\n" + "(Click To Clear)");
    }

    //Update the secondDate var with the date entered
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateSecondDate(LocalDate secondDate) {
        this.secondDate = secondDate;
        DateTimeFormatter formatterForEnteredDate = DateTimeFormatter.ofPattern("d/M/yyyy");
        secondDateView.setText(secondDate.format(formatterForEnteredDate) + "\n" + "(Click To Clear)");
    }


    //Search Between the Requested dates for the Key EQuakes
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onClickSearch(View aview) {

        List<EQuakeEntry> EQuakeDataInTime = new ArrayList<>();

        //ensure that a valid date/dates have been entered to search with
        if (firstDate != null) {
            for (EQuakeEntry quake : EQuakeData) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss");
                LocalDateTime quakeDateTime = LocalDateTime.parse(quake.getPubDate(), formatter);

                DateTimeFormatter formatterForEnteredDate = DateTimeFormatter.ofPattern("d M yyyy");
                LocalDate quakeDate = LocalDate.from(quakeDateTime);


                if (secondDateView.getText().equals("- To The Date (Optional)")) {
                    if (quakeDate.isEqual(firstDate)) {
                        EQuakeDataInTime.add(quake);
                    }
                } else if (quakeDate.isAfter(firstDate) && quakeDate.isBefore(secondDate) || quakeDate.isEqual(firstDate) || quakeDate.isEqual(secondDate)) {
                    EQuakeDataInTime.add(quake);
                }
            }
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Please Enter A Search Date", Toast.LENGTH_SHORT).show();
        }

        //First check of there ware any earthquakes on or between the specified date/dates
        if (EQuakeDataInTime.size() > 0) {
            double northerly = EQuakeDataInTime.get(0).getLatitude();
            double southerly = EQuakeDataInTime.get(0).getLatitude();
            double westerly = EQuakeDataInTime.get(0).getLongitude();
            double easterly = EQuakeDataInTime.get(0).getLongitude();
            double largest = 0;
            double deepest = 0;
            double shallowest = 6371;

            EQuakeEntry mostNortherlyQuake = EQuakeDataInTime.get(0);
            EQuakeEntry mostSoutherlyQuake = EQuakeDataInTime.get(0);
            EQuakeEntry mostWesterlyQuake = EQuakeDataInTime.get(0);
            EQuakeEntry mostEasterlyQuake = EQuakeDataInTime.get(0);
            EQuakeEntry mostLargestQuake = EQuakeDataInTime.get(0);
            EQuakeEntry mostDeepestQuake = EQuakeDataInTime.get(0);
            EQuakeEntry mostShallowestQuake = EQuakeDataInTime.get(0);

            //Search through the data and find the Key earthquakes (northerly,southerly,westerly,easterly,largest,deepest,shallowest)
            for (EQuakeEntry quake : EQuakeDataInTime) {
                String EQDescription = quake.getDescription();
                String[] descriptionSections = EQDescription.split(" ; ");
                String[] depthParts = descriptionSections[3].split(": ");
                String[] magnatudeParts = descriptionSections[4].split(": ");
                String[] depth = depthParts[1].split(" km");


                if (quake.getLatitude() > northerly) {
                    northerly = quake.getLatitude();
                    mostNortherlyQuake = quake;
                }
                if (quake.getLatitude() < southerly) {
                    southerly = quake.getLatitude();
                    mostSoutherlyQuake = quake;
                }
                if (quake.getLongitude() < westerly) {
                    westerly = quake.getLongitude();
                    mostWesterlyQuake = quake;
                }
                if (quake.getLongitude() > easterly) {
                    easterly = quake.getLongitude();
                    mostEasterlyQuake = quake;
                }
                if (Double.parseDouble(magnatudeParts[1]) > largest) {
                    largest = Double.parseDouble(magnatudeParts[1]);
                    mostLargestQuake = quake;
                }
                if (Double.parseDouble(depth[0]) > deepest) {
                    deepest = Double.parseDouble(depth[0]);
                    mostDeepestQuake = quake;
                }
                if (Double.parseDouble(depth[0]) < shallowest) {
                    shallowest = Double.parseDouble(depth[0]);
                    mostShallowestQuake = quake;
                }
            }
            //save the filtered data to a new ArrayList
            EQuakeDataInTimeOrdered.clear();
            mostNortherlyQuake.setExtra("mostNortherlyQuake");
            EQuakeDataInTimeOrdered.add(mostNortherlyQuake);
            mostSoutherlyQuake.setExtra("mostSoutherlyQuake");
            EQuakeDataInTimeOrdered.add(mostSoutherlyQuake);
            mostWesterlyQuake.setExtra("mostWesterlyQuake");
            EQuakeDataInTimeOrdered.add(mostWesterlyQuake);
            mostEasterlyQuake.setExtra("mostEasterlyQuake");
            EQuakeDataInTimeOrdered.add(mostEasterlyQuake);
            mostLargestQuake.setExtra("mostLargestQuake");
            EQuakeDataInTimeOrdered.add(mostLargestQuake);
            mostDeepestQuake.setExtra("mostDeepestQuake");
            EQuakeDataInTimeOrdered.add(mostDeepestQuake);
            mostShallowestQuake.setExtra("mostShallowestQuake");
            EQuakeDataInTimeOrdered.add(mostShallowestQuake);

            //Display the filtered earthquake data to the user
            CustomAdapterForFilter adapter = new CustomAdapterForFilter(EQuakeDataInTimeOrdered);
            filterRecyclerView.setAdapter(adapter);
            recyclerView.setVisibility(View.GONE);
            filterRecyclerView.setVisibility(View.VISIBLE);
            searchButton.setVisibility(View.GONE);
            clearButton.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "No EarthQuakes Were Found", Toast.LENGTH_LONG).show();
        }
    }

    //Refreah the List with the current EQUake data
    public void onClickLoadData(View aview) {
        populateRecyclerView();
        filterRecyclerView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        firstDate = null;
        secondDate = null;
    }

    //Load the current EQuake Data in to the CustomAdapter
    public void populateRecyclerView() {
        CustomAdapter adapter = new CustomAdapter(EQuakeData);
        recyclerView.setAdapter(adapter);
    }

    //Save the Current Values to the bundle when device rotates
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!EQuakeData.isEmpty()){
            outState.putSerializable("MainFragmentsEquakeData", (Serializable) EQuakeData);
        }
        if (!EQuakeDataInTimeOrdered.isEmpty()){
            outState.putSerializable("MainFragmentsFilteredEQuakeData", (Serializable) EQuakeDataInTimeOrdered);
        }
        outState.putSerializable("firstDate", (Serializable) firstDate);
        outState.putSerializable("secondDate", (Serializable) secondDate);
    }

}

