package pl.edu.pw.aasd.data;

import pl.edu.pw.aasd.Jsonable;

import java.util.Date;

public class UserVote extends Jsonable {
    String userId;
    PetrolPrice petrolPrice;
    Date timestamp;

    public UserVote() {
    }

    static public UserVote from(String json) {
        return gson.fromJson(json, UserVote.class);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public PetrolPrice getPetrolPrice() {
        return petrolPrice;
    }

    public void setPetrolPrice(PetrolPrice petrolPrice) {
        this.petrolPrice = petrolPrice;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
