package pl.edu.pw.aasd.data;

import pl.edu.pw.aasd.Jsonable;

public class PartnerPromotion extends Jsonable {
    String id;
    String description;

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
