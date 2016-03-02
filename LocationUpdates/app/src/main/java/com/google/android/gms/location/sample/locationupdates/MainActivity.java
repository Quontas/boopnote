package com.google.android.gms.location.sample.locationupdates;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Registers the user (if they haven't registered already. Otherwise, the user is immediately forwarded to NoteUI.class
 */
public class MainActivity extends AppCompatActivity{

    // UI Widgets.
    protected TextView mTxtDisplay; //TextView is Android's way of displaying uneditable (as far as I know) text.  Used for displaying information.
    protected EditText mRegisterSlot;   //EditText is Android's way of displaying editable text.  This is where the user puts their name in to receive an AuthToken that is then stored.
    protected Button mRegisterSubmitButton; //Gets the text stored in EditText at that current time, and submits it to loud.red
    public SharedPreferences settings;  //This is crucial for the app's function. SharedPreferences are settings that pertain to this app that are stored in between usages and classes (this is used for the AuthToken as of now)
    private static final String TAG = MainActivity.class.getName(); //Used for Tag.i(...), Android's version of System.out.println()
    public static final String PREFS_NAME = "derp"; //The name of the SharedPreferences file.  Don't ask.
    String key; //The AuthToken returned by loud.red
    @Override
    public void onCreate(Bundle savedInstanceState) {   //Initialization
        super.onCreate(savedInstanceState);
        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);  //Retrieves the SharedPreferences.
        key = settings.getString("key", "");    //if no "key" token can be found in SharedPreferences, then key is set to the value of an empty String (used for confirming pre-registration)
        if (!key.equals("")) {  //Has the user registered already?
            Intent intent = new Intent(this, NoteUI.class); //If so, switch to NoteUI.class
            startActivity(intent);
            finish();   //Prevents the user from coming back to this class and registering over and over and over.  However, also prevents other users from registering on this device.  Don't see why the latter is needed, though.
        }
        //Otherwise, get 'em registered!
        setContentView(R.layout.main_activity); //Sets the UI to the layout described in main_activity.xml


        // Locate the UI widgets on main_activity.xml.  All values returned by R.id.*** are integers, findViewById(R.id.***) sets the UI widget in Java to correlate with the element in XML
        mRegisterSlot = (EditText) findViewById(R.id.register); //sets mRegisterSlot to correlate with the <EditText> found in main_activity.xml
        mRegisterSubmitButton = (Button) findViewById(R.id.submit_Registration);    //sets mRegisterSubmitButton to correlate with the <Button> found in main_activity.xml
        mTxtDisplay =  (TextView) findViewById(R.id.textView);  //sets mTextView to correlate with the TextView found in main_activity.xml

        // Set labels.

        mRegisterSlot.setEnabled(true); //Makes it so that the user can write in mRegisterSlot
        mRegisterSubmitButton.setOnClickListener(new View.OnClickListener() {   //sets mRegisterSubmitButton to correlate with another <Button> element in main_activity.xml
            public void onClick(View v) {   //when clicked
                register(mRegisterSlot.getText().toString());
            }
        });

    }

    /**
     * Submits the user's name to loud.red and sets
     * the response to the user's ID and stores it in
     * SharedPreferences as their key
     * @param name
     */
    public void register(String name) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);  //Dunno how this works
        String url ="https://bn.loud.red/register?name="+name;  //Concatenated URL
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject jObj = new JSONObject(response); //Get response from URL
                            String s = jObj.get("token").toString();    //Get the AuthToken from the response
                            //Edit SharedPreferences settings
                            SharedPreferences.Editor edit = settings.edit();
                            key = s;
                            edit.clear();
                            //Store it
                            edit.putString("key", key);
                            //Store it permanently
                            edit.commit();
                            //Proof success
                            Log.i(TAG, "registerKey = " + s);
                            Log.i(TAG, "Success!");
                        } catch (JSONException e) {
                            //You fucked shit up
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //You fucked shit up
                mTxtDisplay.setText("That didn't work!");
            }
        });
        queue.add(stringRequest); //Not much of an explanation here, dunno
        queue.start();  //Also dunno
        Intent intent = new Intent(this, NoteUI.class); //Prepare the switch in classes from MainActivity.class to NoteUI.class
        startActivity(intent);  //Switcherino
        finish(); //Prevent user from coming back here
    }
}
