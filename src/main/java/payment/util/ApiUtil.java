package payment.util;

import org.json.JSONObject;
import payment.enums.HttpRequestMethod;
import payment.util.classes.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ApiUtil {
    // CONFIG: configure TicketServiceURL accordingly
    public static String API_KEY = "paymentServerApiToken";
    public static String TicketServiceURL = "http://booking-app:3000";
    public static String TicketWebhookToken;

    public static Response call(String apiUrl, HttpRequestMethod method) throws IOException{
        return call(apiUrl, method, null, null);
    }

    public static Response call(String apiUrl, HttpRequestMethod method, String apiKey) throws IOException{
        return call(apiUrl, method, null, apiKey);
    }

    public static Response call(String apiUrl, HttpRequestMethod method, JSONObject postData) throws IOException{
        return call(apiUrl, method, postData, null);
    }

    public static Response call(String apiUrl, HttpRequestMethod method, JSONObject postData, String apiKey) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(method.getString());

        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
        if (apiKey != null){
            connection.setRequestProperty("API-Key", apiKey);
        }
        if (postData != null) {
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Write data to the request body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = postData.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        int responseCode = 0;
        BufferedReader reader = null;
        try {
            responseCode = connection.getResponseCode();
            if(connection.getInputStream() != null)
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } catch (Exception e) {
            System.out.println("API responded with an error response");
            if(connection.getErrorStream() != null)
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        }

        System.out.println("API responded with response code: " + responseCode);

        StringBuilder response = new StringBuilder();
        String line;

        if (reader != null){
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
        }

        connection.disconnect();

        JSONObject json = new JSONObject();

        Object data = null;
        try{
            json = new JSONObject(response.toString());
            data = json.get("data");
        } catch (Exception e){
            System.out.println("API response does not have any data");
        }

        try {
            return new Response(
                    (String) json.get("message"),
                    (boolean) json.get("valid"),
                    data);
        } catch (Exception e) {
            System.out.println("API response is unstandardized");
            return new Response(
                    "Non standard response",
                    false,
                    response.toString());
        }
    }
}
