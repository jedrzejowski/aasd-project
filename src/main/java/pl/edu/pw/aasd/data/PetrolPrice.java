package pl.edu.pw.aasd.data;

import pl.edu.pw.aasd.Jsonable;

public class PetrolPrice extends Jsonable {

    public String pb98;
    public String pb95;

    public PetrolPrice() {
    }

    static public PetrolPrice from(String json) {
        return gson.fromJson(json, PetrolPrice.class);
    }

    public String getPb98() {
        return pb98;
    }

    public void setPb98(String pb98) {
        this.pb98 = pb98;
    }

    public String getPb95() {
        return pb95;
    }

    public void setPb95(String pb95) {
        this.pb95 = pb95;
    }

}
