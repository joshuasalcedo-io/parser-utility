package io.joshuasalcedo.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

public class JsonUtils {
    private static final Gson gson = new GsonBuilder().create();
    private static final Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Convert any object to a JSON string
     */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }


    /**
     * Convert any object to a JSON string with pretty printing
     */
    public static String toPrettyJson(Object obj) {
        return prettyGson.toJson(obj);
    }

    /**
     * Convert any object to a JSON Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toJsonMap(Object obj) {
        String json = gson.toJson(obj);
        return gson.fromJson(json, Map.class);
    }
}
