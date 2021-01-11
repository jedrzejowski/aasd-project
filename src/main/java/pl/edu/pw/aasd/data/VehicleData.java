package pl.edu.pw.aasd.data;

import pl.edu.pw.aasd.Jsonable;

public class VehicleData extends Jsonable {
    float latitude = 0;
    float longitude = 0;
    float fuelLeft = 0;
    float fuelPerKilometer = 0;

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

    public float getFuelLeft() {
        return fuelLeft;
    }

    public void setFuelLeft(float fuelLeft) {
        this.fuelLeft = fuelLeft;
    }

    public float getFuelPerKilometer() {
        return fuelPerKilometer;
    }

    public void setFuelPerKilometer(float fuelPerKilometer) {
        this.fuelPerKilometer = fuelPerKilometer;
    }
}
