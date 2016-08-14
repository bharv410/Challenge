package challenge.skurt.com.skurtchallenge;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface FlightStatsService {

    @GET("flex/flightstatus/rest/v2/json/flight/status/761435187?appId=91b929e6&appKey=2eebba75c50ce13c31b9ef0b331fb93a")
    Call<ResponseBody> getFlightStats();

}
