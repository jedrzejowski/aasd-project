package pl.edu.pw.aasd;

import com.google.gson.Gson;

import java.util.Collection;

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

    static public <T extends Jsonable> String toJson(Collection<T> list) {
        return gson.toJson(list.stream().map(Jsonable::toJSON).toArray());
    }

    static public <T extends Jsonable> String toJson(Object obj) {
        return gson.toJson(obj);
    }
}
