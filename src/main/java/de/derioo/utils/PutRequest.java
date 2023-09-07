package de.derioo.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PutRequest {


    private final String url;

    public PutRequest(String url) {
        this.url = url;
    }

    public int execute(String data) {
        try {
            URL url = new URL(this.url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("PUT");

            connection.setRequestProperty("Accept-Charset", "UTF-8");

            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = data.getBytes("utf-8");
                os.write(input, 0, input.length);
            }


            int responseCode = connection.getResponseCode();

            return responseCode;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

}
