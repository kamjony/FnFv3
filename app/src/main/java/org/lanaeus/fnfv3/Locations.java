package org.lanaeus.fnfv3;

/**
 * Created by KamrulHasan on 3/19/2018.
 */

public class Locations {
    private String lat,lng;

    private Locations() {

    }

    public Locations(String lat, String lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}
