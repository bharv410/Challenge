package challenge.skurt.com.skurtchallenge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
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


            Call<ResponseBody> myCall = flightStatsService.getFlightStats();
            try {
                Response<ResponseBody> response = myCall.execute();
                System.out.println("response code" + response.code());
                System.out.println("response message" + response.message());
                System.out.println("response body" + response.body().string());
                System.out.println("response raw" + response.raw().toString());

            }catch (IOException ioexception){

            }

//            Call<List<FlightStat>> flightStatsCall = flightStatsService.getFlightStats();
//            try {
//                Response<List<FlightStat>> flightStatsResponse = flightStatsCall.execute();
//                System.out.println("resppone1 = " + flightStatsResponse.raw().message());
//
//                System.out.println("resppone2 = " + flightStatsResponse.message());
//
//                System.out.println("resppone3 = " + flightStatsResponse.raw().body().string());
//
//                System.out.println("resppone4 = " + String.valueOf(flightStatsResponse.raw().code()));
//                List<FlightStat> flightStats = flightStatsResponse.body();
//                for(FlightStat flightStat : flightStats){
//                    System.out.println("Flight id = " + flightStat.flightId);
//                }
//
//            }catch (IOException ioException){
//
//            }

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
