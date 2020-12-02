package pl.edu.pw.aasd;

import com.google.gson.Gson;

public class Jsonable {
    public static Gson gson = new Gson();

    public String toJSON() {
        return gson.toJson(this);
    }

    static public <T> T from(String json, Class<T> c) {
        return gson.fromJson(json, c);
    }

    @Override
    public String toString() {
        return toJSON();
    }
}
