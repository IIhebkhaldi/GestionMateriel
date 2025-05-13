// InfobipSmsService.java
package tn.esprit.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.cdimascio.dotenv.Dotenv;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Service for sending SMS messages through Infobip API v2 (Advanced SMS endpoint).
 * Reads configuration from .env (INFOBIP_BASE_URL, INFOBIP_API_KEY) and trims whitespace.
 * Automatically prefixes 'https://' if missing in INFOBIP_BASE_URL.
 */
public class InfobipSmsService {
    private final HttpClient client;
    private final String baseUrl;
    private final String apiKey;

    public InfobipSmsService() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String rawUrl = dotenv.get("INFOBIP_BASE_URL");
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new IllegalStateException("INFOBIP_BASE_URL must be set in environment or .env file");
        }
        rawUrl = rawUrl.trim();
        if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
            rawUrl = "https://" + rawUrl;
        }
        this.baseUrl = rawUrl;

        String rawKey = dotenv.get("INFOBIP_API_KEY");
        if (rawKey == null || rawKey.isBlank()) {
            throw new IllegalStateException("INFOBIP_API_KEY must be set in environment or .env file");
        }
        this.apiKey = rawKey.trim();

        this.client = HttpClient.newHttpClient();

        System.out.println("[InfobipSmsService] Base URL: " + baseUrl);
        System.out.println("[InfobipSmsService] API Key prefix: " + apiKey.substring(0, 8) + "...");
    }

    /**
     * Send SMS using Infobip Advanced endpoint
     * @param from Sender ID or phone number
     * @param to   Recipient in E.164 format (+...)
     * @param text Message content
     * @throws Exception on HTTP or API error
     */
    public void sendSms(final String from, final String to, final String text) throws Exception {
        if (from == null || from.isBlank()) throw new IllegalArgumentException("Sender 'from' must not be empty.");
        if (to == null || to.isBlank()) throw new IllegalArgumentException("Recipient 'to' must not be empty.");
        if (text == null || text.isBlank()) throw new IllegalArgumentException("Message 'text' must not be empty.");

        // Construct message
        JsonObject message = new JsonObject();
        message.addProperty("from", from);
        message.addProperty("text", text);

        JsonArray destinations = new JsonArray();
        JsonObject dest = new JsonObject();
        String formattedTo = to.startsWith("+") ? to : "+" + to;
        dest.addProperty("to", formattedTo);
        destinations.add(dest);
        message.add("destinations", destinations);

        JsonArray messages = new JsonArray();
        messages.add(message);

        JsonObject payload = new JsonObject();
        payload.add("messages", messages);

        // Build request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/sms/2/text/advanced"))
                .header("Authorization", "App " + apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        System.out.println("[InfobipSmsService] Sending request to: " + request.uri());
        System.out.println("[InfobipSmsService] Payload: " + payload);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("[InfobipSmsService] Response code: " + response.statusCode());
        System.out.println("[InfobipSmsService] Response body: " + response.body());

        if (response.statusCode() == 401) {
            throw new RuntimeException("Infobip SMS unauthorized (401): please verify your API key and Base URL.");
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Infobip SMS error (" + response.statusCode() + "): " + response.body());
        }
    }
}

