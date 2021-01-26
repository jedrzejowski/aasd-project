package pl.edu.pw.aasd.data;

import pl.edu.pw.aasd.Jsonable;

import java.util.ArrayList;

public class PartnerPromotion extends Jsonable {
    String id;
    String description;
    int maxReservations = 0;
    ArrayList<String> userIds = new ArrayList<>();

    public int getMaxReservations() {
        return maxReservations;
    }

    public void setMaxReservations(int maxReservations) {
        this.maxReservations = maxReservations;
    }

    public int getActualReservations() {
        return userIds.size();
    }

    public boolean addUserToPromotion(String user) {
        if (userIds.size() == maxReservations) {
            return false;
        }
        userIds.add(user);
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
