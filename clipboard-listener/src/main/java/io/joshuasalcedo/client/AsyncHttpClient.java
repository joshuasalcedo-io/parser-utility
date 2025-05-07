package io.joshuasalcedo.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AsyncHttpClient {
    private final HttpClient client;

    public AsyncHttpClient() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Sends an asynchronous POST request with JSON payload to the specified URL.
     *
     * @param url The URL to send the request to
     * @param jsonPayload The JSON payload to send
     * @return A CompletableFuture with the response body as a String
     */
    public CompletableFuture<String> post(String url, String jsonPayload) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    /**
     * Checks if a server is available by sending a HEAD request to the URL.
     *
     * @param url The URL to check
     * @param timeoutSeconds Timeout in seconds
     * @return true if the server is available, false otherwise
     */
    public boolean isServerAvailable(String url, int timeoutSeconds) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .build();

            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() < 400;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
}