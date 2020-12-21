package pl.edu.pw.aasd;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.Collection;

public class Jsonable {
    public static Gson gson = new Gson();

    public JsonElement toJson() {
        return gson.toJsonTree(this);
    }

    static public <T> T from(String json, Class<T> c) {
        return gson.fromJson(json, c);
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }

    static public <T extends Jsonable> String toString(Collection<T> list) {
        return gson.toJson(list.stream().map(Object::toString).toArray());
    }

    static public <T extends Jsonable> String toString(Object obj) {
        return gson.toJson(obj);
    }

    static public JsonElement toJson(Object obj) {
        return gson.toJsonTree(obj);
    }
}
