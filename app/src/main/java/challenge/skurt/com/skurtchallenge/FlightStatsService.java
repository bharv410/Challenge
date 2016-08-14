package challenge.skurt.com.skurtchallenge;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface FlightStatsService {

    @GET("flex/flightstatus/rest/v2/json/airport/status/OKBK/dep/2013/2/27/13?appId=16decbe1&appKey=2ed0ada41c7ef6c31253ce11c93b8692&utc=false&numHours=6&maxFlights=10")
    Call<ResponseBody> getFlightStats();

}
