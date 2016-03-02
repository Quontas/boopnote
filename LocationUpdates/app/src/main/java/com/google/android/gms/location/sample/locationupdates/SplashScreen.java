package com.google.android.gms.location.sample.locationupdates;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * The first screen any user (registered or not) will be presented with upon entering DropANote.
 * SplashScreen serves no purpose besides being a pretty presentation point.
 */
public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {    //Serves as the "constructor". Any and all Activity objects must contain a onCreate(Bundle savedInstanceState) method.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);    //Sets the user interface to the interface described by activity_splash_screen.xml

        Button fab = (Button) findViewById(R.id.nextButton);    //The button that progresses the user from the SplashScreen to MainActivity.class (if unregistered) or NoteUI.class (if already registered)
        fab.setOnClickListener(new View.OnClickListener() { //Creates an action that occurs when "Next" is clicked.
            @Override
            public void onClick(View view) {    //Actual method that creates the action. "view" is the current activity (so in this case, SplashScreen)
                nextScreen(view);   //Performs nextScreen()
            }
        });

    }

    /**
     * Goes to the next screen (MainActivity.class if registered, NoteUI.class if unregistered)
     * @param view
     */
    public void nextScreen(View view)   {
        Intent intent = new Intent(this, MainActivity.class);   //Intent is the class that demonstrates that the user intends (hence the name) to go to another Activity.
        startActivity(intent); //transfers the user             // Intents have a beginning and endpoint, and in this case (and most other cases), the beginning is this current class
                                                                //and the end is another class (in this case, MainActivity.class)
    }

}
