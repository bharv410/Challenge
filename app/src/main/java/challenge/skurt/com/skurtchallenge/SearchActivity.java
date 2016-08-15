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
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
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
    private static final int SEARCH_SLIDE_ANIMATION_DISTANCE = 2000;

    private EditText flightNumberEditText;
    private TextView bottomTextView;
    private RelativeLayout searchLayout;
    private RelativeLayout flightInfoSkurtLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        flightNumberEditText = (EditText) findViewById(R.id.flightNumberEditText);
        bottomTextView = (TextView) findViewById(R.id.bottomTextView);
        searchLayout = (RelativeLayout) findViewById(R.id.search_layout);
        flightInfoSkurtLayout = (RelativeLayout) findViewById(R.id.flightInfoSkurtLayout);

        flightNumberEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    String flightNumberText = flightNumberEditText.getText().toString();

                    FlightStatsAsyncTask flightStatsAsyncTask = new FlightStatsAsyncTask(getApplicationContext(), SearchActivity.this, flightNumberText);
                    flightStatsAsyncTask.execute();

                }
                return false;
            }
        });
    }

    private class FlightStatsAsyncTask extends AsyncTask<Void, Void, String> {
        private final Context context;
        private final SearchActivity searchActivity;
        private final String flightNumberString;
        private boolean exceptionOccurred = false;

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

                if(flightStats == null || flightStats.flightStatus == null){
                    this.exceptionOccurred = true;
                    return "Flight not found";
                }

                String timestamp = flightStats.flightStatus.operationalTimes.publishedArrival.dateUtc;

                DateTime dateTime = ISODateTimeFormat.dateTimeParser().parseDateTime(timestamp);

                String dateString =  "day " + dateTime.dayOfMonth().getAsText() + " mont " + dateTime.monthOfYear().getAsText()+ " year" + dateTime.year().getAsText();

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
                        "Could not find flight number",
                        Toast.LENGTH_SHORT);

                toast.show();
            } else {


                final Toast toast = Toast.makeText(this.context, result,
                        Toast.LENGTH_SHORT);

                toast.show();

                searchActivity.searchLayout.animate().translationY(-1 * SEARCH_SLIDE_ANIMATION_DISTANCE);
                searchActivity.flightInfoSkurtLayout.setVisibility(View.VISIBLE);
                searchActivity.flightInfoSkurtLayout.animate().translationY(-400);
            }
        }
    }
}
