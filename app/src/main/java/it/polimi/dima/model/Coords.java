package it.polimi.dima.model;

/**
 * Created by Davide on 17/12/2016.
 */

public class Coords {


    private Float latitude;
    private Float longitude;



    public Float getLatitude() {
        return latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setCoords(Float latitude, Float longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setCoords(Double latitude, Double longitude) {
        this.latitude = latitude.floatValue();
        this.longitude = longitude.floatValue();
    }

}
