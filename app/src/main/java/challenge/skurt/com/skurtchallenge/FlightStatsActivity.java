package challenge.skurt.com.skurtchallenge;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FlightStatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_stats);

        FlightStatsAsyncTask flightStatsAsyncTask = new FlightStatsAsyncTask(getApplicationContext(), this);
        flightStatsAsyncTask.execute();
    }

    private class FlightStatsAsyncTask extends AsyncTask<Void, Void, Void> {
        private final Context context;
        private final Activity activityToFinish;
        private boolean exceptionOccurred = false;
        private Exception exception = null;

        public FlightStatsAsyncTask(final Context context, final Activity activityToFinish) {
            this.context = context.getApplicationContext();
            this.activityToFinish = activityToFinish;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.flightstats.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            FlightStatsService flightStatsService = retrofit.create(FlightStatsService.class);

            Call<FlightStatusResponse> flightStatsCall = flightStatsService.getFlightStats(761435187);
            try {
                Response<FlightStatusResponse> flightStatsResponse = flightStatsCall.execute();

                FlightStatusResponse flightStats = flightStatsResponse.body();

                String timestamp = flightStats.flightStatus.operationalTimes.publishedArrival.dateUtc;

                DateTime dateTime = ISODateTimeFormat.dateTimeParser().parseDateTime(timestamp);

                System.out.println("benmark Flight arrival = " + "day " + dateTime.dayOfMonth().getAsText() + " mont " + dateTime.monthOfYear().getAsText()+ " year" + dateTime.year().getAsText());

            }catch (IOException ioException){

            }

            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            super.onPostExecute(result);

            if (this.exceptionOccurred) {
                final Toast toast = Toast.makeText(this.context,
                        "Sorry. Please try again when" + " you have an internet connection",
                        Toast.LENGTH_SHORT);

                toast.show();
            } else {
                final Toast toast = Toast.makeText(this.context,
                        "Done",
                        Toast.LENGTH_SHORT);

                toast.show();
            }
        }
    }
}
