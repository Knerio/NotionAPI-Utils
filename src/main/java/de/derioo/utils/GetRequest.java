package de.derioo.utils;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class GetRequest {

    private final String url;
    private final Map<String, String> headers = new HashMap<>();

    public GetRequest(String url) {
        this.url = url;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public String execute(String token) {
        try {
            URL url = new URL(this.url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Accept-Charset", "UTF-8");

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            int responseCode = connection.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {

                response.append(inputLine);
            }
            in.close();

            return response.toString();

        } catch (
                IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
