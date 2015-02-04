package com.trysurfer.surfer.user;

import android.location.Location;

/**
 * Created by PRO on 11/1/2014.
 */
public class UserLocation {
    private Location userLocation;

    public UserLocation(Location location){
        setUserLocation(location);
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    public Location getUserLocation(){
        return userLocation;
    }
}
