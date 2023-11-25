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
    public static String TicketServiceURL = "http://[::1]:3100";
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

        connection.setRequestProperty("Authorization", "Bearer paymentServerApiToken");
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

        try {
            JSONObject json = new JSONObject(response.toString());
            return new Response(
                    (String) json.get("message"),
                    (boolean) json.get("valid"),
                    json.get("data")
                    );
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("API responded unstandardized data");
            return new Response("API responded unstandardized data", false, response.toString());
        }
    }
}
