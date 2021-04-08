package org.euan.equake;

import android.content.Context;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

//Euan McDonald
//StudentID - s1927457
public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private static List<EQuakeEntry> localDataSet = new ArrayList<>();
    double maxMagnatude = 0;
    private Context context;
    private  CustomAdapter.ReturnSelectedQuake ReturnSelectedQuakeCallback;

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView quakeExtra;
        private final LinearLayout elementLayout;
        private final TextView quakeLocation;
        private final TextView quakeMagnitude;
        private final TextView quakeTime;


        public ViewHolder(View view) {
            super(view);

            //Setup an OnClickListener for the ViewHolder as to allow for the clicking of list items
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ReturnSelectedQuakeCallback.updateSelectedQuake(localDataSet.get(getAdapterPosition()));

                }
            });
            quakeExtra = (TextView) view.findViewById(R.id.extraElement);
            elementLayout = (LinearLayout) view.findViewById(R.id.elementLayout);
            quakeLocation = (TextView) view.findViewById(R.id.quakeLocation);
            quakeMagnitude = (TextView) view.findViewById(R.id.quakeMagnitude);
            quakeTime = (TextView) view.findViewById(R.id.quakeTime);

        }

        public LinearLayout getElementLayout() {
            return elementLayout;
        }
        public TextView getQuakeExtra() {
            return quakeExtra;
        }
        public TextView getQuakeLocation() {
            return quakeLocation;
        }
        public TextView getQuakeMagnitude() {
            return quakeMagnitude;
        }
        public TextView getQuakeTime() {
            return quakeTime;
        }
    }

    //Constructor Which accepts a list of EQuakes
    public CustomAdapter(List<EQuakeEntry> dataSet) {

        localDataSet = dataSet;

        for(EQuakeEntry coord : localDataSet) {
            String EQDescription = coord.getDescription();
            String[] descriptionSections = EQDescription.split(" ; ");
            String[] magnatudeParts = descriptionSections[4].split(": ");
            if (Double.parseDouble(magnatudeParts[1]) > maxMagnatude){
                maxMagnatude = Double.parseDouble(magnatudeParts[1]);
            }

        }
    }

    //Set up the ViewHolder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recycler_view_item, viewGroup, false);
        context = viewGroup.getContext();

        try {
            ReturnSelectedQuakeCallback = (CustomAdapter.ReturnSelectedQuake ) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnDateSetListener");
        }

        return new ViewHolder(view);
    }

    //Populate the ViewHolder with the EQuake Data list
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        String EQDescription = localDataSet.get(position).getDescription();
        String[] descriptionSections = EQDescription.split(" ; ");

        viewHolder.getQuakeLocation().setText(descriptionSections[1]);
        viewHolder.getQuakeMagnitude().setText(descriptionSections[4]);
        String[] magnatudeParts = descriptionSections[4].split(": ");


        //Colour code the entry into quartile groups based of the highest recoded magnitude
        String entryColour = "#809400D3";
        if (Double.parseDouble(magnatudeParts[1])/maxMagnatude <= 0.25){
            entryColour = "#80228B22";
        }
        else if(Double.parseDouble(magnatudeParts[1])/maxMagnatude > 0.25 && Double.parseDouble(magnatudeParts[1])/maxMagnatude <= 0.5){
            entryColour = "#80FFFF00";
        }
        else if(Double.parseDouble(magnatudeParts[1])/maxMagnatude > 0.5 && Double.parseDouble(magnatudeParts[1])/maxMagnatude <= 0.75){
            entryColour = "#80FF8C00";
        }
        else if(Double.parseDouble(magnatudeParts[1])/maxMagnatude > 0.75){
            entryColour = "#80B22222";
        }

        viewHolder.getQuakeTime().setText("Time Recorded: " + localDataSet.get(position).getPubDate());
        viewHolder.getElementLayout().setBackgroundColor(Color.parseColor(entryColour));

    }

    //Get the count of items in the ViewHolder
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }


    //Find the selected item
    public interface ReturnSelectedQuake {
        // Declaration of the template function for the interface
        void updateSelectedQuake(EQuakeEntry EQuake);
    }


}

