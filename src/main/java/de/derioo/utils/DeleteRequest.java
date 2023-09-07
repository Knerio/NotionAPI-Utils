package de.derioo.utils;

import lombok.AllArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class DeleteRequest {

    private final String url;
    private final String body;
    private final Map<String, String> headers = new HashMap<>();

    public void addHeaders(String key, String value) {
        headers.put(key, value);
    }

    public String execute(String token) {


        try {
            URL url = new URL(this.url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setDoOutput(true);

            connection.setRequestProperty("Authorization", "Bearer " + token);

            // Setzen Sie die hinzugefügten Header
            for (Map.Entry<String, String> entry : this.headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            // Write the request body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = body.getBytes("UTF-8");
                os.write(input, 0, input.length);
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
