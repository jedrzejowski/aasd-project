package pl.edu.pw.aasd.data;

import pl.edu.pw.aasd.Jsonable;

public class Near extends Jsonable {
    float latitude = 0;
    float longitude = 0;
    float distance = 0;

    public Near(float latitude, float longitude, float distance) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
