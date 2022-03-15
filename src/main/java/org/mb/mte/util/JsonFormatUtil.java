package org.mb.mte.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonFormatUtil {

    public static String getJson(String rawData){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        assert rawData != null;
        JsonElement je = JsonParser.parseString(rawData);
        return gson.toJson(je);
    }
}
