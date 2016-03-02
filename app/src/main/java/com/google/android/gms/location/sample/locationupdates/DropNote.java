package com.google.android.gms.location.sample.locationupdates;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

public class DropNote extends NoteUI {
    protected Location mCurrentLocation;    //CurrentLocation

    protected Button mDropNote; //Submit button
    protected TextView mMessage;    //Oh... Now I remember... EditText is editable, as is TextView.

    String key; //The all-important key.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);    //Gettin the prefs
        SharedPreferences mPrefs = getSharedPreferences("derp", 0); //Dunno, don't ask.
        key = mPrefs.getString("key", "");  //Set the local key to the AuthToken
        setContentView(R.layout.activity_drop_note);    //Set the UI
        mMessage = (TextView) findViewById(R.id.message);   //Set up the TextView
        mMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {   //Hide the keyboard if the user clicks outside of the keyboard
                if (!hasFocus) {    //If there is a tap on the screen, and it's not the keyboard
                    hideKeyboard(v);    //Hide it
                }
            }
        });

        mDropNote = (Button) findViewById(R.id.dropNote);   //Set up the Submit button
        mDropNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dropNote();
            }
        }); //if the button's clicked, then do dropNote()
        updateValuesFromBundle(savedInstanceState); //Updating CurrentLocation
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }
    /**
     * Sends a POST request to loud.red, and adds a Note at this location.
     * Makes toast!
     */
    public void dropNote()  {
        if (key == "" || mMessage.getText().toString().length() == 0)  {    //If the key doesn't exist, or the message is blank, then you don't get to send
            //Do nothing
        }
        else {
            RequestQueue queue = Volley.newRequestQueue(this);  //Again, idk man
            String url = "https://bn.loud.red/add?text=" + mMessage.getText().toString() + "&long=" + mCurrentLocation.getLongitude() + "&lat=" + mCurrentLocation.getLatitude() + "&token=" + key; //Concatenate the URL
            Log.i(TAG, "url = " + url); //Print it
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Display the first 500 characters of the response string.
                            try {   //If it ain't in here, you done messed it up
                                JSONObject jObj = new JSONObject(response); //Store the response temporarily
                                Log.i(TAG, "Success! id = " + jObj.toString(2));    //Done.
                            } catch (JSONException e) {
                                //Ya dun messed up
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });
            queue.add(stringRequest);
            queue.start();
            Context context = getApplicationContext();  //Get the context of the app
            CharSequence text = "Dropped!"; //What's on the toast?
            int duration = Toast.LENGTH_SHORT;  //How long to show toast?

            Toast toast = Toast.makeText(context, text, duration);  //Make toast!
            toast.show();   //Toast!
        }
    }

    /**
     * Update the current location
     * @param savedInstanceState
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
        }
    }

    /**
     * Just hides the keyboard.
     * @param view
     */
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * On startup, connect to the API
     */
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    /**
     * On shutdown, disconnect from the API
     */
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public void onConnected(Bundle connectionHint) {
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
    }
}
