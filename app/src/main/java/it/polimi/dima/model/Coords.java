package it.polimi.dima.model;

/**
 * Created by Davide on 17/12/2016.
 */

public class Coords {


    private Double latitude;
    private Double longitude;

    public Coords (Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }


    public void setCoords(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
