package pl.edu.pw.aasd.data;

import pl.edu.pw.aasd.Jsonable;

public class StationDescription extends Jsonable {
    String logo = "";
    String commonName = "";
    String description = "";

    public StationDescription() {
    }

    static public StationDescription from(String json) {
        return gson.fromJson(json, StationDescription.class);
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
