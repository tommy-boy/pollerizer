package com.geoblender.pollerizer;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private static String SERVICE_URL = "http://geoblender.com/android/android.php";
    private static final String SERVICE_URL_Y = "http://geoblender.com/android/yes.php";
    private static final String SERVICE_URL_N = "http://geoblender.com/android/no.php";
    // 1. Declare a service url:
    private static final String SERVICE_URL_SURVEYS = "http://www.geoblender.com/android/survey.php";

    private static final String LOG_TAG = "Pollerizer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button yesButton = (Button) findViewById(R.id.buttonYes);
        final Button noButton = (Button) findViewById(R.id.buttonNo);

        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.mainLayout);




        // Font path
        String fontBold = "fonts/FuturaTodayScreenBold.ttf";
        String fontLight = "fonts/FuturaTodayScreenLight.ttf";

        // text view labels
        TextView txtQuestion = (TextView) findViewById(R.id.textView);
        TextView txtYes = (TextView) findViewById(R.id.lblYes);
        TextView txtNo = (TextView) findViewById(R.id.lblNo);
        TextView txtVotesYes = (TextView) findViewById(R.id.votesYes);
        TextView txtVotesNo = (TextView) findViewById(R.id.votesNo);
        TextView txtPercentYes = (TextView) findViewById(R.id.percentYes);
        TextView txtPercentNo = (TextView) findViewById(R.id.percentNo);
        TextView txtButtonYes = (TextView) findViewById(R.id.buttonYes);
        TextView txtButtonNo = (TextView) findViewById(R.id.buttonNo);

        // Loading Font Face
        Typeface tfb = Typeface.createFromAsset(getAssets(), fontBold);
        Typeface tfl = Typeface.createFromAsset(getAssets(), fontLight);

        // Applying font
        txtQuestion.setTypeface(tfb);
        txtYes.setTypeface(tfb);
        txtNo.setTypeface(tfb);
        txtVotesYes.setTypeface(tfl);
        txtVotesNo.setTypeface(tfl);
        txtPercentYes.setTypeface(tfl);
        txtPercentNo.setTypeface(tfl);
        txtButtonYes.setTypeface(tfb);
        txtButtonNo.setTypeface(tfb);








        getData(); // getData for initial page load

        // 1. Create a Yes vote and call castVote:
        View.OnClickListener yes = new View.OnClickListener() {
            int counter = 0;
            public void onClick(View view) {
                String myVote = "Yes";
                castVote(myVote);
                counter ++;
                Toast.makeText(getApplicationContext(), "Yes vote #" + counter + " sent to server",
                        Toast.LENGTH_SHORT).show();
            }
        };



        // 1. Create a No vote and call castVote:
        View.OnClickListener no = new View.OnClickListener() {
            int counter = 0;
            public void onClick(View view) {
                String myVote = "No";
                castVote(myVote);
                counter ++;
                Toast.makeText(getApplicationContext(), "No vote #" + counter + " sent to server",
                        Toast.LENGTH_SHORT).show();
            }
        };

        yesButton.setOnClickListener(yes);
        noButton.setOnClickListener(no);
    } // onCreate



    // 2. Receive vote and call vote() and getData():
    private void castVote(final String myVote) {
        // Worker thread cuz it's a network operation.
        new Thread(new Runnable() {
            public void run() {
                try {
                    vote(myVote);
                    getData(); // refresh data after voting
                    Log.e(LOG_TAG, myVote);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Cannot vote", e);
                    return;
                }
            }
        }).start();
    }



    // 3. Send vote to server:
    protected void vote(final String myVote) throws IOException {
        HttpURLConnection conn = null;
        final StringBuilder json = new StringBuilder();
        try {
            // Connect to the web service

            if(myVote == "Yes"){
                SERVICE_URL = SERVICE_URL_Y;
                Log.e(LOG_TAG, SERVICE_URL);
            } else {
                SERVICE_URL = SERVICE_URL_N;
                Log.e(LOG_TAG, SERVICE_URL);
            }

            URL url = new URL(SERVICE_URL);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream()); // Sends vote

            // Read the JSON data into the StringBuilder
            // int read;
            // char[] buff = new char[1024];
            // while ((read = in.read(buff)) != -1) {
            //    json.append(buff, 0, read);
            // }

        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "No connection",
                    Toast.LENGTH_SHORT).show();
            Log.e(LOG_TAG, "Error connecting to service", e);
            throw new IOException("Error connecting to service", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    } // connect to web



    // 4. Create function to call retrieveData:
    private void getData() {
        // Retrieve the city data from the web service
        // In a worker thread since it's a network operation.
        new Thread(new Runnable() {
            public void run() {
                try {
                    retrieveData();
                    Log.e(LOG_TAG, "Data retrived");
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Cannot retrive data", e);
                    return;
                }
            }
        }).start();
    }



    // 5. Create function to retrieve JSON and call processJSON:
    protected void retrieveData() throws IOException {
        HttpURLConnection conn = null;
        final StringBuilder json = new StringBuilder();
        try {
            // Connect to the web service
            URL url = new URL(SERVICE_URL_SURVEYS);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Read the JSON data into the StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                json.append(buff, 0, read);
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to service", e);
            throw new IOException("Error connecting to service", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        // Do stuff with JSON
        // Must run this on the UI thread since it's a UI operation.
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    processJson(json.toString());
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error processing JSON", e);
                }
            }
        });
    }



    // 6. Process JSON and display results:
    void processJson(String json) throws JSONException {
        // De-serialize the JSON string into an array of objects
        JSONArray jsonArray = new JSONArray(json);
        for (int i = 0; i < jsonArray.length(); i++) {

            JSONObject jsonObj = jsonArray.getJSONObject(i);

            double totalVotes = (int) jsonObj.getDouble("yes") + (int) jsonObj.getDouble("no");
            double percentYes = (int) jsonObj.getDouble("yes") / totalVotes * 100;
            double percentNo = (int) jsonObj.getDouble("no") / totalVotes * 100;
            percentYes = Math.round(percentYes*10.0)/10.0;
            percentNo = Math.round(percentNo*10.0)/10.0;

            //Log.e(LOG_TAG, "Stuff: " + percentYes);

            TextView labelVotesYes = (TextView) findViewById(R.id.votesYes);
            TextView labelVotesNo = (TextView) findViewById(R.id.votesNo);
            TextView labelPercentYes = (TextView) findViewById(R.id.percentYes);
            TextView labelPercentNo = (TextView) findViewById(R.id.percentNo);

            labelVotesYes.setText(jsonObj.getString("yes"));
            labelVotesNo.setText(jsonObj.getString("no"));
            labelPercentYes.setText(String.valueOf(percentYes));
            labelPercentNo.setText(String.valueOf(percentNo));

        } // loop
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
