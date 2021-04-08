package org.euan.equake;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

//Euan McDonald
//StudentID - s1927457
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private LocalDate firstDate;
    private LocalDate secondDate;
    private DatePickerFragment.ReturnFirstDate ReturnFirstDateCallback;
    private DatePickerFragment.ReturnSecondDate ReturnSecondDateCallback;
    private Context context;

    //Set up the DialogFragment with its Callbacks
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        try {
            ReturnFirstDateCallback = (DatePickerFragment.ReturnFirstDate) context;
            ReturnSecondDateCallback = (DatePickerFragment.ReturnSecondDate) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnDateSetListener");
        }
    }

    //Save the entered first date to the bundle
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static DatePickerFragment newInstance(LocalDate firstDate) {
        Bundle args = new Bundle();
        args.putSerializable("firstDate", (Serializable) firstDate);
        DatePickerFragment f = new DatePickerFragment();
        f.setArguments(args);
        return f;
    }

    //Create and display the data picker dialogs
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        if (getTag().equals("firstDatePicker")) {
            DatePickerDialog pickerDisplay = new DatePickerDialog(getActivity(), this, year, month, day);
            return pickerDisplay;
        } else {
            //Grey out the dates before the First selected date
            firstDate = (LocalDate) getArguments().getSerializable("firstDate");
            ZonedDateTime HideDate = firstDate.atStartOfDay(ZoneId.systemDefault());
            long milliSinceEpoch = HideDate.toInstant().toEpochMilli();
            DatePickerDialog pickerDisplay = new DatePickerDialog(getActivity(), this, year, month, day);
            pickerDisplay.getDatePicker().setMinDate(milliSinceEpoch);
            return pickerDisplay;
        }

    }

    //Handle what happens to the selected date
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onDateSet(DatePicker view, int year, int month, int day) {

        String dateInStringFormat = String.valueOf(day) + " " + String.valueOf(month + 1) + " " + String.valueOf(year);

        DateTimeFormatter formatterForEnteredDate = DateTimeFormatter.ofPattern("d M yyyy");
        LocalDate dateEntered = LocalDate.parse(dateInStringFormat, formatterForEnteredDate);

        //If the user chose the first date send the date via the updateFirstDate interface, else send via the updateSecondDate
        if (getTag().equals("firstDatePicker")) {
            firstDate = dateEntered;
            ReturnFirstDateCallback.updateFirstDate(dateEntered);
        } else {
            secondDate = dateEntered;
            ReturnSecondDateCallback.updateSecondDate(dateEntered);
        }
    }

    //Send the selected date back using an interface
    public interface ReturnFirstDate {
        void updateFirstDate(LocalDate firstDate);
    }

    //Send the selected date back using an interface
    public interface ReturnSecondDate {
        void updateSecondDate(LocalDate secondDate);
    }





}






















