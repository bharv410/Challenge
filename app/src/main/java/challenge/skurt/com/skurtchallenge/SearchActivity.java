package challenge.skurt.com.skurtchallenge;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchActivity extends AppCompatActivity {
    private static final int SEARCH_SLIDE_ANIMATION_DISTANCE = -2000;
    private static final int CONFIRMATION_SLIDE_ANIMATION_DISTANCE = -400;

    private EditText flightNumberEditText;
    private RelativeLayout searchLayout;
    private RelativeLayout flightInfoSkurtLayout;
    private TextView confirmationTextView;
    private Button noButton;
    private Button yesButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        this.flightNumberEditText = (EditText) findViewById(R.id.flightNumberEditText);
        this.searchLayout = (RelativeLayout) findViewById(R.id.search_layout);
        this.flightInfoSkurtLayout = (RelativeLayout) findViewById(R.id.flightInfoSkurtLayout);
        this.confirmationTextView = (TextView) findViewById(R.id.confirmationTextView);
        this.noButton = (Button) findViewById(R.id.noButton);
        this.yesButton = (Button) findViewById(R.id.yesButton);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);


        OnNoClickListener onNoClickListener = new OnNoClickListener(this);
        this.noButton.setOnClickListener(onNoClickListener);

        OnYesClickListener onYesClickListener = new OnYesClickListener(this);
        this.yesButton.setOnClickListener(onYesClickListener);

        OnDoneClickListener onDoneClickListener = new OnDoneClickListener(this);
        this.flightNumberEditText.setOnEditorActionListener(onDoneClickListener);
    }

    private void goToConfirmation(String confirmationString){
        this.searchLayout.animate().translationY(SEARCH_SLIDE_ANIMATION_DISTANCE);
        this.flightNumberEditText.setText("");
        this.flightInfoSkurtLayout.setVisibility(View.VISIBLE);
        this.flightInfoSkurtLayout.animate().translationY(CONFIRMATION_SLIDE_ANIMATION_DISTANCE);
        this.confirmationTextView.setText(confirmationString);
    }

    private void goBackToSearch(){
        this.searchLayout.animate().translationY(0);
        this.flightInfoSkurtLayout.setVisibility(View.GONE);
        this.flightInfoSkurtLayout.animate().translationY(0);
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
        protected void onPreExecute() {
            super.onPreExecute();
            this.searchActivity.progressBar.setVisibility(View.VISIBLE);
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
                    return getString(R.string.flight_not_found);
                }

                String timestamp = flightStats.flightStatus.operationalTimes.publishedArrival.dateUtc;
                DateTime dateTime = ISODateTimeFormat.dateTimeParser().parseDateTime(timestamp);
                DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm:ssa");

                return getString(R.string.flight_info, dtf.print(dateTime));
            }catch (IOException ioException){
                this.exceptionOccurred = true;
                return getString(R.string.connectivity_issues);
            }
        }

        @Override
        protected void onPostExecute(final String result) {
            super.onPostExecute(result);
            this.searchActivity.progressBar.setVisibility(View.GONE);

            if (this.exceptionOccurred) {
                final Toast toast = Toast.makeText(this.context,
                        result,
                        Toast.LENGTH_LONG);

                toast.show();
            } else {
                this.searchActivity.goToConfirmation(result);
            }
        }
    }

    private class OnNoClickListener implements View.OnClickListener{
        private final SearchActivity searchActivity;
        public OnNoClickListener(SearchActivity searchActivity){
            this.searchActivity = searchActivity;

        }

        @Override
        public void onClick(View v) {
            this.searchActivity.goBackToSearch();
        }
    }

    private class OnYesClickListener implements View.OnClickListener{
        private final SearchActivity searchActivity;
        public OnYesClickListener(SearchActivity searchActivity){
            this.searchActivity = searchActivity;

        }
        private void goBackToSearch(){
            this.searchActivity.goBackToSearch();
        }

        @Override
        public void onClick(View v) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(searchActivity);
            alertBuilder.setMessage(getString(R.string.confirm_skurt));
            alertBuilder.setCancelable(false);
            alertBuilder.setPositiveButton(
                    getString(R.string.okay_thanks),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            goBackToSearch();
                        }
                    });

            AlertDialog alertDialog = alertBuilder.create();
            alertDialog.show();
        }
    }

    private class OnDoneClickListener implements TextView.OnEditorActionListener{
        private final SearchActivity searchActivity;
        public OnDoneClickListener(SearchActivity searchActivity){
            this.searchActivity = searchActivity;

        }
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                String flightNumberText = this.searchActivity.flightNumberEditText.getText().toString();

                FlightStatsAsyncTask flightStatsAsyncTask = new FlightStatsAsyncTask(getApplicationContext(), SearchActivity.this, flightNumberText);
                flightStatsAsyncTask.execute();

            }
            return false;
        }
    }
}
