package org.euan.equake;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


//Euan McDonald
//StudentID - s1927457
public class MainActivity extends AppCompatActivity implements DatePickerFragment.ReturnFirstDate, DatePickerFragment.ReturnSecondDate, CustomAdapter.ReturnSelectedQuake, CustomAdapterForFilter.ReturnSelectedQuake {


    private MainFragment MainFragment;
    private MapViewFragment MapViewFragment;
    private String urlSource = "http://quakes.bgs.ac.uk/feeds/MhSeismology.xml";
    private List<EQuakeEntry> EQuakeData = new ArrayList<>();
    private Fragment currentFragment;
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    //onCreate method of the MainActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MapViewFragment = new MapViewFragment();

        //load in any InstanceState data
        if (savedInstanceState != null) {

            //Restore the fragment's instance
            currentFragment = getSupportFragmentManager().getFragment(savedInstanceState, "myFragmentName");
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.fragmentContainer, currentFragment, "currentFragment").commit();
            EQuakeData = (List<EQuakeEntry>) savedInstanceState.getSerializable("AllEQuakeData");
            MainFragment = new MainFragment();
            if (currentFragment != null && currentFragment instanceof MainFragment) {
                MainFragment = (MainFragment) currentFragment;
            }
            MainFragment.ReceiveQuakeData(EQuakeData);


        } else {
            MainFragment = new MainFragment();
            startProgress();
        }

        executor.scheduleWithFixedDelay(new Runnable(){

            public void run() {
                if (currentFragment != null && currentFragment instanceof MainFragment) {
                    new DownloadAndUpdateEQuakeData().execute(urlSource);
                }
            }

        }, 5, 5, TimeUnit.MINUTES);

        BottomNavigationView navigationView = findViewById(R.id.bottomNavigationView);
        navigationView.setOnNavigationItemSelectedListener(navigationListener);

    }

    //Save the Current Values to the bundle when device rotates
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, "myFragmentName", currentFragment);
        outState.putSerializable("AllEQuakeData", (Serializable) EQuakeData);
    }

    //Stop the refresh thread when the app is closed
    @Override
    protected void onStop() {
        executor.shutdown();
        System.out.println("Finish");
        super.onStop();
    }

    //Handle the click input for the bottom navigation bar
    private BottomNavigationView.OnNavigationItemSelectedListener navigationListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();

            switch (item.getItemId()) {
                case R.id.eQuakes:
                    if (manager.getBackStackEntryCount() > 0) {
                        manager.popBackStackImmediate();
                    } else {
                    }
                    MainFragment.ReceiveQuakeData(EQuakeData);
                    selectedFragment = MainFragment;
                    currentFragment = MainFragment;
                    break;
                case R.id.map:
                    MapViewFragment.updateMapData(EQuakeData);
                    selectedFragment = MapViewFragment;
                    currentFragment = MapViewFragment;
                    break;
            }

            transaction.replace(R.id.fragmentContainer, selectedFragment, "currentFragment");
            transaction.commit();
            return true;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onClickSearch(View v) {
        MainFragment.onClickSearch(v);
    }

    //Navigate the user to the EQuake Details page and load on the data of the Selected EQuake
    public void updateSelectedQuake(EQuakeEntry EQuake) {
        DetailsViewFragment DetailsViewFragment = new DetailsViewFragment();
        DetailsViewFragment.updateDetailsData(EQuake);
        currentFragment = DetailsViewFragment;
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, DetailsViewFragment, "currentFragment");
        transaction.commit();
    }

    //Show the action bar
    public void showActionBarBack() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //Hide the action bar
    public void hideActionBarBack() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    //Handle what hapens when the back ke is pressed
    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        currentFragment = fragmentManager.findFragmentByTag("currentFragment");
        //If the user is on the details fragment divert them to the main fragment, else close the app
        if (currentFragment != null && currentFragment instanceof DetailsViewFragment) {
            MainFragment.ReceiveQuakeData(EQuakeData);
            currentFragment = MainFragment;
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.fragmentContainer, MainFragment, "currentFragment");
            transaction.commit();
        } else {
            super.onBackPressed();
        }
    }

    //Trigger the clear function when the clear button is cliked
    public void onClickClear(View v) {
        MainFragment.onClickClear(v);
    }

    //Send the selected firstDate Received from the DatePicker Fragment to the main fragment via a callback
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateFirstDate(LocalDate firstDate) {
        MainFragment.updateFirstDate(firstDate);
    }

    //Send the selected SecondDate Received from the DatePicker Fragment to the main fragment via a callback
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateSecondDate(LocalDate SecondDate) {MainFragment.updateSecondDate(SecondDate); }

    //Trigger the AsyncTask
    @SuppressWarnings("deprecation")
    public void startProgress() {
        new DownloadEQuakeData().execute(urlSource);
    }


    @SuppressWarnings("deprecation")
    //Load in the data from the URL
    private class DownloadEQuakeData extends AsyncTask<String, Void, String> {
        private String result = "";
        protected String doInBackground(String... prams) {
            URL aurl;
            URLConnection yc;
            BufferedReader in = null;
            String inputLine = "";

            try {
                aurl = new URL(prams[0]);
                yc = aurl.openConnection();
                in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

                int stage = 0;
                while ((inputLine = in.readLine()) != null) {
                    if (stage > 12 || stage < 3 && stage > 0) {
                        result = result + inputLine;
                        stage++;
                    } else {
                        stage++;
                    }
                }
                in.close();
                return result;
            } catch (IOException ae) {
                return null;
            }
        }


        protected void onPostExecute(String result) {
            parseTheData(result);
        }
    }

    //Parse the data received from the URL and create EQuake objects from it
    public void parseTheData(String loadedData) {
        System.out.println(loadedData);
        List<EQuakeEntry> listOfCoords = new ArrayList<>();
        try {
            EQuakeEntry itemEntry = null;

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(loadedData));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                } else if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equalsIgnoreCase("item")) {
                        itemEntry = new EQuakeEntry();
                    } else if (xpp.getName().equalsIgnoreCase("title")) {
                        String title = xpp.nextText();
                        itemEntry.setTitle(title);
                    } else if (xpp.getName().equalsIgnoreCase("description")) {
                        String description = xpp.nextText();
                        itemEntry.setDescription(description);
                    } else if (xpp.getName().equalsIgnoreCase("link")) {
                        String link = xpp.nextText();
                        itemEntry.setLink(link);
                    } else if (xpp.getName().equalsIgnoreCase("pubDate")) {
                        String pubDate = xpp.nextText();
                        itemEntry.setPubDate(pubDate);
                    } else if (xpp.getName().equalsIgnoreCase("category")) {
                        String category = xpp.nextText();
                        itemEntry.setCategory(category);
                    } else if (xpp.getName().equalsIgnoreCase("lat")) {
                        String latitude = xpp.nextText();
                        itemEntry.setLatitude(Double.parseDouble(latitude));
                    } else if (xpp.getName().equalsIgnoreCase("long")) {
                        String longitude = xpp.nextText();
                        itemEntry.setLongitude(Double.parseDouble(longitude));
                    } else {

                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (xpp.getName().equalsIgnoreCase("item")) {
                        listOfCoords.add(itemEntry);
                    }
                }
                eventType = xpp.next();
            }
            EQuakeData = listOfCoords;
            //send the data to the MainFragment and then Transaction to that fragment
            MainFragment.ReceiveQuakeData(EQuakeData);
            currentFragment = MainFragment;
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, MainFragment, "currentFragment").commit();

        } catch (XmlPullParserException | IOException error) {
            error.printStackTrace();
        }
    }




    @SuppressWarnings("deprecation")
    //Load in the data from the URL
    private class DownloadAndUpdateEQuakeData extends AsyncTask<String, Void, String> {
        private String result = "";
        protected String doInBackground(String... prams) {
            URL aurl;
            URLConnection yc;
            BufferedReader in = null;
            String inputLine = "";

            try {
                aurl = new URL(prams[0]);
                yc = aurl.openConnection();
                in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

                int stage = 0;
                while ((inputLine = in.readLine()) != null) {
                    if (stage > 12 || stage < 3 && stage > 0) {
                        result = result + inputLine;
                        stage++;
                    } else {
                        stage++;
                    }
                }
                in.close();
                return result;
            } catch (IOException ae) {
                return null;
            }
        }


        protected void onPostExecute(String result) {
            parseTheDataAndUpdate(result);
        }
    }

    //Parse the data received from the URL and create EQuake objects from it
    public void parseTheDataAndUpdate(String loadedData) {
        System.out.println(loadedData);
        List<EQuakeEntry> listOfCoords = new ArrayList<>();
        try {
            EQuakeEntry itemEntry = null;

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(loadedData));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                } else if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equalsIgnoreCase("item")) {
                        itemEntry = new EQuakeEntry();
                    } else if (xpp.getName().equalsIgnoreCase("title")) {
                        String title = xpp.nextText();
                        itemEntry.setTitle(title);
                    } else if (xpp.getName().equalsIgnoreCase("description")) {
                        String description = xpp.nextText();
                        itemEntry.setDescription(description);
                    } else if (xpp.getName().equalsIgnoreCase("link")) {
                        String link = xpp.nextText();
                        itemEntry.setLink(link);
                    } else if (xpp.getName().equalsIgnoreCase("pubDate")) {
                        String pubDate = xpp.nextText();
                        itemEntry.setPubDate(pubDate);
                    } else if (xpp.getName().equalsIgnoreCase("category")) {
                        String category = xpp.nextText();
                        itemEntry.setCategory(category);
                    } else if (xpp.getName().equalsIgnoreCase("lat")) {
                        String latitude = xpp.nextText();
                        itemEntry.setLatitude(Double.parseDouble(latitude));
                    } else if (xpp.getName().equalsIgnoreCase("long")) {
                        String longitude = xpp.nextText();
                        itemEntry.setLongitude(Double.parseDouble(longitude));
                    } else {

                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (xpp.getName().equalsIgnoreCase("item")) {
                        listOfCoords.add(itemEntry);
                    }
                }
                eventType = xpp.next();
            }
            EQuakeData.clear();
            EQuakeData = listOfCoords;
            //send the data to the MainFragment
            MainFragment.updateQuakeData(EQuakeData);

        } catch (XmlPullParserException | IOException error) {
            error.printStackTrace();
        }
    }

}