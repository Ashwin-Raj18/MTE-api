package org.mb.mte.util;

import com.google.gson.*;

import java.util.List;

public class JsonFormatUtil {

    public static String getJson(String rawData){
        Gson gson = new GsonBuilder().create();
        assert rawData != null;
        JsonElement je = JsonParser.parseString(rawData);
        return gson.toJson(je);
    }

    public static JsonObject getJsonObject(String jsonData){
        return new Gson().fromJson(jsonData, JsonObject.class);
    }

    public static String getKeyValueJsonString(String jsonData,String key){
        return new Gson().fromJson(jsonData, JsonObject.class).get(key).getAsString();
    }

    public static String listToJson(List<?> rawData){
        Gson gson = new Gson();
        String jsonCartList = gson.toJson(rawData);
        return jsonCartList;
    }

}
