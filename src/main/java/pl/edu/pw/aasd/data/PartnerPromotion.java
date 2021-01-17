package pl.edu.pw.aasd.data;

import pl.edu.pw.aasd.Jsonable;

public class PartnerPromotion extends Jsonable {
    String id;
    String description;
    int maxReservations = 0;
    int actualReservations = 0;

    public int getMaxReservations() {
        return maxReservations;
    }

    public void setMaxReservations(int maxReservations) {
        this.maxReservations = maxReservations;
    }

    public int getActualReservations() {
        return actualReservations;
    }

    public void setActualReservations(int actualReservations) {
        this.actualReservations = actualReservations;
    }

    public boolean incrementReservations(){
        if (actualReservations == maxReservations)
            return false;
        ++actualReservations;
        return true;
    }

    public PartnerPromotion(String id) {
        this.id = id;
    }

    static public PartnerPromotion from(String json) {
        return gson.fromJson(json, PartnerPromotion.class);
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
