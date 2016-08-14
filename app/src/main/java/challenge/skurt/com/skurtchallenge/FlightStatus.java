package challenge.skurt.com.skurtchallenge;

import com.google.gson.annotations.SerializedName;

public class FlightStatus {

    @SerializedName("flightId")
    public Integer flightId;

    @SerializedName("operationalTimes")
    public OperationalTimes operationalTimes;
}
