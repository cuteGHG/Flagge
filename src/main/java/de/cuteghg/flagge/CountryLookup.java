package de.cuteghg.flagge;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

final class CountryLookup {
    private final HttpClient client;
    private final Gson gson = new Gson();
    private final String endpoint;
    private final Duration timeout;

    CountryLookup(String endpoint, long timeoutMs) {
        this.endpoint = endpoint;
        this.timeout = Duration.ofMillis(timeoutMs);
        this.client = HttpClient.newBuilder().connectTimeout(this.timeout).build();
    }

    CompletableFuture<PlayerCountry> lookup(String ip) {
        String url = endpoint.replace("%ip%", ip);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(timeout)
                .header("Accept", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> parse(response.statusCode(), response.body()));
    }

    private PlayerCountry parse(int statusCode, String body) {
        if (statusCode < 200 || statusCode >= 300) {
            throw new IllegalStateException("Geo-IP API returned HTTP " + statusCode);
        }

        try {
            JsonObject json = gson.fromJson(body, JsonObject.class);
            if (json == null || !"success".equalsIgnoreCase(json.get("status").getAsString())) {
                throw new IllegalStateException("Geo-IP API did not return success");
            }

            String country = json.has("country") ? json.get("country").getAsString() : "";
            String code = json.has("countryCode") ? json.get("countryCode").getAsString() : "";
            if (country.isBlank() || code.isBlank()) {
                throw new IllegalStateException("Geo-IP API returned incomplete data");
            }
            return new PlayerCountry(country, code.toUpperCase());
        } catch (JsonParseException | IllegalStateException | UnsupportedOperationException exception) {
            throw new IllegalStateException("Invalid Geo-IP response", exception);
        }
    }
}
