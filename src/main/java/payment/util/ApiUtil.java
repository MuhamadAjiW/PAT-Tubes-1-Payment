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

    public static Response call(String apiUrl, HttpRequestMethod method, JSONObject postData) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(method.getString());

        if (postData != null) {
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Write data to the request body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = postData.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        connection.setRequestProperty("Authorization", "Bearer paymentServerApiToken");

        int responseCode = 0;
        BufferedReader reader;
        try {
            responseCode = connection.getResponseCode();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } catch (Exception e) {
            System.out.println("API responded with an error response");
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        }

        System.out.println("API responded with response code: " + responseCode);

        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

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
