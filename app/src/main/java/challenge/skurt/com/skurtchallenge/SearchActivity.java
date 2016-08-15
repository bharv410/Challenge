package challenge.skurt.com.skurtchallenge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchActivity extends AppCompatActivity {
    private EditText flightNumberEditText;


    //change this
    public TextView bottomTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        flightNumberEditText = (EditText) findViewById(R.id.flightNumberEditText);
        bottomTextView = (TextView) findViewById(R.id.bottomTextView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String flightNumberText = flightNumberEditText.getText().toString();
                Snackbar.make(view,"Searching for flight number" +  flightNumberText , Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();



                FlightStatsAsyncTask flightStatsAsyncTask = new FlightStatsAsyncTask(getApplicationContext(), SearchActivity.this, flightNumberText);
                flightStatsAsyncTask.execute();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class FlightStatsAsyncTask extends AsyncTask<Void, Void, String> {
        private final Context context;
        private final SearchActivity searchActivity;
        private final String flightNumberString;
        private boolean exceptionOccurred = false;
        private Exception exception = null;

        public FlightStatsAsyncTask(final Context context, final SearchActivity searchActivity,
                                    final String flightNumberString) {
            this.context = context.getApplicationContext();
            this.searchActivity = searchActivity;
            this.flightNumberString = flightNumberString;
        }

        @Override
        protected String doInBackground(final Void... params) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.flightstats.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            FlightStatsService flightStatsService = retrofit.create(FlightStatsService.class);

            Call<FlightStatusResponse> flightStatsCall = flightStatsService.getFlightStats(this.flightNumberString);
            try {
                Response<FlightStatusResponse> flightStatsResponse = flightStatsCall.execute();

                FlightStatusResponse flightStats = flightStatsResponse.body();

                String timestamp = flightStats.flightStatus.operationalTimes.publishedArrival.dateUtc;

                DateTime dateTime = ISODateTimeFormat.dateTimeParser().parseDateTime(timestamp);

                String dateString =  "day " + dateTime.dayOfMonth().getAsText() + " mont " + dateTime.monthOfYear().getAsText()+ " year" + dateTime.year().getAsText();
                System.out.println("benmark Flight arrival = " + dateString);
                return dateString;
            }catch (IOException ioException){

            }

            return null;
        }

        @Override
        protected void onPostExecute(final String result) {
            super.onPostExecute(result);

            if (this.exceptionOccurred) {
                final Toast toast = Toast.makeText(this.context,
                        "Sorry. Please try again when" + " you have an internet connection",
                        Toast.LENGTH_SHORT);

                toast.show();
            } else {

                searchActivity.bottomTextView.setText(result);
            }
        }
    }
}
