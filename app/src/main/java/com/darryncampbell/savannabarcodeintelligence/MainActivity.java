package com.darryncampbell.savannabarcodeintelligence;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnTouchListener(this);

        DataWedgeUtilities.CreateProfiles(getApplicationContext());
    }

    @Override
    protected void onNewIntent(Intent newIntent)
    {
        //  New scan Intent received by DataWedge
        if (newIntent != null)
        {
            String action = newIntent.getAction();
            if (action.equalsIgnoreCase(getPackageName() + ".ACTION"))
            {
                //  Received a barcode through StartActivity
                displayScanResult(newIntent);
            }
        }
    }

    private void displayScanResult(Intent initiatingIntent) {
        String decodedSource = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_source));
        String decodedData = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_data));
        String decodedLabelType = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_label_type));
        //  todo
        final TextView tempOutput = findViewById(R.id.idTemp);
        tempOutput.setText("" + Calendar.getInstance().getTime() + " " + decodedData);
        Log.i("SAVANNA_BI", "Received Scan: " + decodedData);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }



    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
        {
            //  Scan button pressed
            DataWedgeUtilities.SoftScanTrigger(getApplicationContext(), true);
        }
        else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
        {
            //  Scan button released
            DataWedgeUtilities.SoftScanTrigger(getApplicationContext(), false);
        }
        return true;    }
}
