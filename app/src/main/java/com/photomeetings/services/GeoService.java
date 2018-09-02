package com.photomeetings.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.photomeetings.model.Point;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public abstract class GeoService {

    public static String reverseGeocoding(Float lat, Float lng) throws IOException {
        String formattedAddress = "";
        if (lat != null && lng != null) {
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://geocode-maps.yandex.ru/1.x/?geocode=" + lng + "," + lat + "&format=json")
                    .build();

            Response response = okHttpClient.newCall(request).execute();
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                JsonParser jsonParser = new JsonParser();
                formattedAddress = jsonParser.parse(responseBody.string()).getAsJsonObject()
                        .getAsJsonObject("response")
                        .getAsJsonObject("GeoObjectCollection")
                        .getAsJsonArray("featureMember").get(0).getAsJsonObject()
                        .getAsJsonObject("GeoObject")
                        .getAsJsonObject("metaDataProperty")
                        .getAsJsonObject("GeocoderMetaData")
                        .getAsJsonObject("Address")
                        .get("formatted").getAsString();
                responseBody.close();
            }
            response.close();
        }
        return formattedAddress;
    }

    public static List<Point> geocoding(String address) throws IOException {
        List<Point> result = new ArrayList<>();
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://geocode-maps.yandex.ru/1.x/?geocode=" + URLEncoder.encode(address, "UTF-8") + "&format=json")
                .build();

        Response response = okHttpClient.newCall(request).execute();
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            JsonParser jsonParser = new JsonParser();
            JsonArray results = jsonParser.parse(responseBody.string()).getAsJsonObject()
                    .getAsJsonObject("response")
                    .getAsJsonObject("GeoObjectCollection")
                    .getAsJsonArray("featureMember");
            for (int i = 0; i < results.size(); i++) {
                JsonObject geoObject = results.get(i).getAsJsonObject().getAsJsonObject("GeoObject");
                String formattedAddress = geoObject.getAsJsonObject("metaDataProperty")
                        .getAsJsonObject("GeocoderMetaData")
                        .getAsJsonObject("Address")
                        .get("formatted").getAsString();
                String pos = geoObject.getAsJsonObject("Point").get("pos").getAsString();
                float lng = Float.parseFloat(pos.split(" ")[0]);
                float lat = Float.parseFloat(pos.split(" ")[1]);
                result.add(new Point(lat, lng, formattedAddress));
            }
            responseBody.close();
        }
        response.close();
        return result;
    }

}
