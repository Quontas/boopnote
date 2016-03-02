package com.google.android.gms.location.sample.locationupdates;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Loads a note and more specific information
 */
public class ReadingNote extends NoteUI {
    protected TextView mContents;   //Contents of the message
    protected TextView mUser;   //Who posted the message?
    protected TextView mTimeCreated;    //When was it posted?
    protected TextView mGetBoops;   //How many boops?
    protected Button mBoop; //Boop it!
    protected JSONObject item;  //The object passed from NoteUI.class
    public void onCreate(Bundle savedInstanceState) {
        try {
            item = new JSONObject(getIntent().getStringExtra("item"));  //Retrieve the JSONObject from NoteUI.class
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG, item.toString());    //Print it.
        super.onCreate(savedInstanceState); //That's it
        setContentView(R.layout.activity_reading_note);

        mContents = (TextView) findViewById(R.id.textView2);
        try {
            mContents.setText(item.getString("text"));  //Set mContents to the text of the JSONObject
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mUser = (TextView) findViewById(R.id.userView);
        try {
            mUser.setText("User: " + item.getString("author")); //Set mUser to the user.
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mTimeCreated = (TextView) findViewById(R.id.timeView);
        long unixSeconds = 0;
        try {
            unixSeconds = Long.parseLong(item.getString("created").toString()); //Show the time created
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mGetBoops = (TextView) findViewById(R.id.textView3);
        try {
            mGetBoops.setText("Boops: " + item.getString("boops"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mBoop = (Button) findViewById(R.id.button4);
        mBoop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    boop(); //Boop it
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            String url = "https://bn.loud.red/boop?id="+item.getString("id")+"&token="+ key;
        } catch (JSONException e) {
            e.printStackTrace();
        }


        Date date = new Date(unixSeconds*1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d 'at' hh:mm a"); // the format of your date
        String formattedDate = sdf.format(date);
        mTimeCreated.setText("Created: " + formattedDate);
    }

    public void boop() throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://bn.loud.red/boop?id="+item.getString("id")+"&token="+ key;
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject jObj = new JSONObject(response);
                            Log.i(TAG, jObj.toString());
                        } catch (JSONException e) {
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
        Context context = getApplicationContext();
        CharSequence text = "Booped!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        try {
            mGetBoops.setText("Boops: " + item.getString("boops"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
