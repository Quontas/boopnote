package com.google.android.gms.location.sample.locationupdates;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
public class NoteUI extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    protected static final String TAG = "location-updates-sample";
    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    public String key;
    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;

    protected TextView mLastUpdateTimeTextView; //TextView that displays the last time the list of notes was updated
    protected TextView mLatitudeTextView;   //TextView that displays the last latitude recorded
    protected TextView mLongitudeTextView;  //TextView that displays the last longitude recorded
    protected ListView mListView;   //ListView shows the list of notes
    protected Button mDropNote; //Button that takes user from NoteUI.class to DropNote.class
    protected Button mReadNotes;    //Button that updates the list of notes
    protected Switch mStartStopSwitch;  //Turns location-tracking on or off

    // Labels.
    protected String mLatitudeLabel;
    protected String mLongitudeLabel;
    protected String mLastUpdateTimeLabel;
    public JSONObject jObj;

    JSONArray jArr;
    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_ui);
        SharedPreferences mPrefs = getSharedPreferences("derp", 0); //Accessing SharedPrefs
        key = mPrefs.getString("key", "");  //Get the AuthToken and store it in this activity
        Log.i(TAG, "key = " + key); //Prove it
        // Locate the UI widgets.
        /**
         * If this switch is ON, then location-tracking is on. If not on, no tracking.
         */
        mStartStopSwitch = (Switch) findViewById(R.id.switch1);
        mStartStopSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {  //Enable checking
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {    //Check if changed
                if (isChecked) {
                    startUpdatesSwitchHandler();    //Sets the location services from off to on
                } else {
                    stopUpdatesSwitchHandler();     //Sets the location services from on to off
                }
            }
        });
        mDropNote = (Button) findViewById(R.id.dropNote);
        mDropNote.setOnClickListener(new View.OnClickListener() {   //Enable clicking on this button
            @Override
            public void onClick(View view) {    //What do if click?
                dropNote(view); //Drop a note!
            }
        });
        mReadNotes = (Button) findViewById(R.id.button2);   //
        mReadNotes.setOnClickListener(new View.OnClickListener() {  //Enable clicking on this button
            @Override
            public void onClick(View view) {    //What do if click?
                readNotes(mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude()); //Fetch the list of notes in the vicinity of currentLocation
            }
        });
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);    //Set up latitude text
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);  //Set up longitude text
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);  //Set up last update time
        mListView = (ListView) findViewById(R.id.listView); //Set up ListView
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            /**
             * Detects if the item in ListView is clicked
             */
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Intent intent = new Intent(view.getContext(), ReadingNote.class);
                String s = null;    //Initialize s
                //All the try/catches in this program do the same thing: check for JSONExceptions or VolleyExceptions.  Nothing special, no real results
                try {
                    s = jArr.getJSONObject(position).toString();    //Set s to the String representation of the item at this list
                } catch (JSONException e) {
                    //Ya dun fucked up
                    e.printStackTrace();
                }
                Log.i(TAG, s);
                intent.putExtra("item",s);  //Transfer the JSONObject string to the next class (only base objects can be transferred between classes
                startActivity(intent);  //Switch classes from NoteUI.class to ReadingNote.class

            }
        });


            // Set labels.
        mLatitudeLabel = getResources().getString(R.string.latitude_label); //Default label
        mLongitudeLabel = getResources().getString(R.string.longitude_label);   //Default label
        mLastUpdateTimeLabel = getResources().getString(R.string.last_update_time_label);   //Default label


        mRequestingLocationUpdates = false; //Yes, yes, default
        mLastUpdateTime = "";   //Yes, yes, default

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            updateUI();
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Switches classes from NoteUI.class to DropNote.class
     * @param view
     */
    public void dropNote(View view) {
        Intent intent = new Intent(this, DropNote.class);   //Prepare for transfer
        startActivity(intent);  //Transfer
    }

    /**
     * Handles the Start Updates button and requests start of location updates. Does nothing if
     * updates have already been requested.
     */
    public void startUpdatesSwitchHandler() {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;  //Store for next check
            startLocationUpdates(); //Start update
        }
    }

    /**
     * Handles the Stop Updates button, and requests removal of location updates. Does nothing if
     * updates were not previously requested.
     */
    public void stopUpdatesSwitchHandler() {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false; //Store for next check
            stopLocationUpdates();  //Stop update
        }
    }

    /**
     * Requests location updates from the FusedLocationApi.  And makes toast!
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        readNotes(mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude());
        Context context = getApplicationContext();  //I dunno why, but it wants this, and this is the quickest way to get context
        CharSequence text = "Updated!"; //What to show
        int duration = Toast.LENGTH_SHORT;  //How long to show?

        Toast toast = Toast.makeText(context, text, duration);  //Toast is ready!
        toast.show();   //Launch toast
    }
    /**
     * Updates the latitude, the longitude, and the last location time in the UI.
     */
    private void updateUI() {
        mLatitudeTextView.setText(String.format("%s: %f", mLatitudeLabel,
                mCurrentLocation.getLatitude()));
        mLongitudeTextView.setText(String.format("%s: %f", mLongitudeLabel,
                mCurrentLocation.getLongitude()));
        mLastUpdateTimeTextView.setText(String.format("%s: %s", mLastUpdateTimeLabel,
                mLastUpdateTime));
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        client.connect();
        mGoogleApiClient.connect();
    }

    @Override
    /**
     * Needed to satisfy implementation
     */
    public void onResume() {
        super.onResume();
    }

    /**
     * Needed to satisfy implementation
     */
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    /**
     * If the app shuts down, so do location services.  Simple as that.
     */
    protected void onStop() {
        mGoogleApiClient.disconnect();

        super.onStop();
        client.disconnect();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();
        }

        // If the user presses the Start Updates button before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Get all of the notes in the area.  Sends a GET request to loud.red, returns /closeby?
     * @param longit
     * @param latit
     */
    public void readNotes(double longit, double latit) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://bn.loud.red/closeby?long=" + longit + "&lat=" + latit + "&token=" + key;
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            jObj = new JSONObject(response);    //Store response temporarily
                            ArrayList<String> items = new ArrayList<>();    //Make a list of items to display
                            jArr = jObj.getJSONArray("notes");  //Needed because Florian sucks at writing JSONArrays
                            Log.i(TAG, jArr.toString(3));   //For my own reference
                            //Add all the items in the JSONArray to the ArrayList
                            for (int i = 0; i < jArr.length(); i++) {
                                long unixSeconds = Long.parseLong(jArr.getJSONObject(i).get("created").toString()); //Get unixTime
                                Date date = new Date(unixSeconds*1000L); // *1000 is to convert seconds to milliseconds
                                SimpleDateFormat sdf = new SimpleDateFormat("MMM d 'at' hh:mm a"); // the format of your date
                                String formattedDate = sdf.format(date); //Make it purdy
                                String s = jArr.getJSONObject(i).get("text").toString()+"\n" + formattedDate; //Add the message content to the date
                                items.add(s); //Add the information to the list to display in ListView
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(NoteUI.this, R.layout.simple_row, items); //Needed for display's sake.
                            mListView.setAdapter(adapter); //Same
                            Log.i(TAG," done"); //Indicates maximum completion.
                        } catch (JSONException e) { //To satisfy the damn compiler
                            e.printStackTrace();    //The usual response given by the program
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(stringRequest);   //...dunno. Just works.
        queue.start();  //...dunno.
    }
}
