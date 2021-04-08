package org.euan.equake;

import android.content.Context;

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
public class CustomAdapterForFilter extends RecyclerView.Adapter<CustomAdapterForFilter.FilterViewHolder> {
    private static List<EQuakeEntry> localDataSet = new ArrayList<>();
    double maxMagnatude = 0;
    private Context context;
    private  CustomAdapterForFilter.ReturnSelectedQuake ReturnSelectedQuakeCallback;

    public class FilterViewHolder extends RecyclerView.ViewHolder {

        private final TextView quakeExtra;
        private final LinearLayout elementLayout;
        private final TextView quakeLocation;
        private final TextView quakeMagnitude;
        private final TextView quakeTime;

        public FilterViewHolder(View view) {
            super(view);

            //Setup an OnClickListener for the ViewHolder as to allow for the clicking of list items
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();

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
    public CustomAdapterForFilter(List<EQuakeEntry> dataSet) {
        localDataSet = dataSet;
    }

    //Set up the ViewHolder
    @Override
    public CustomAdapterForFilter.FilterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recycler_view_item, viewGroup, false);

        context = viewGroup.getContext();
        try {
            ReturnSelectedQuakeCallback = (CustomAdapterForFilter.ReturnSelectedQuake ) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnDateSetListener");
        }

        return new CustomAdapterForFilter.FilterViewHolder(view);
    }


    //Populate the ViewHolder with the EQuake Data list
    @Override
    public void onBindViewHolder(CustomAdapterForFilter.FilterViewHolder FilterViewHolder, final int position) {


        String EQDescription = localDataSet.get(position).getDescription();
        String[] descriptionSections = EQDescription.split(" ; ");

        FilterViewHolder.getQuakeLocation().setText(descriptionSections[1]);
        FilterViewHolder.getQuakeMagnitude().setText(descriptionSections[4]);
        String[] magnatudeParts = descriptionSections[4].split(": ");

        //Add titles for each of the filtered entries
        switch(position) {
            case 0:
                FilterViewHolder.getQuakeExtra().setText("Most Northerly Earthquake");
                break;
            case 1:
                FilterViewHolder.getQuakeExtra().setText("Most Southerly Earthquake");
                break;
            case 2:
                FilterViewHolder.getQuakeExtra().setText("Most Westerly Earthquake");
                break;
            case 3:
                FilterViewHolder.getQuakeExtra().setText("Most Easterly Earthquake");
                break;
            case 4:
                FilterViewHolder.getQuakeExtra().setText("Largest Magnitude Earthquake");
                break;
            case 5:
                FilterViewHolder.getQuakeExtra().setText("Deepest Earthquake");
                break;
            case 6:
                FilterViewHolder.getQuakeExtra().setText("Shallowest Earthquake");
                break;
            default:
                FilterViewHolder.getQuakeExtra().setText("Error");
        }

        FilterViewHolder.getQuakeTime().setText("Time Recorded: " + localDataSet.get(position).getPubDate());

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

