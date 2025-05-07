package io.joshuasalcedo.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Random;
import java.util.UUID;

public class ClipboardApiDemo {
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final String BASE_URL = "http://localhost:8085";
    private static final String API_PATH = "/api/clipboards"; // Note: plural "clipboards"

    public static void main(String[] args) {
        System.out.println("Starting Clipboard API Demo...");
        System.out.flush(); // Force output to console

        try {
            // Step 1: Create a sample clipboard entry
            System.out.println("Creating clipboard entry...");
            String id1 = createClipboard("This is the first clipboard test entry");
            System.out.println("Created clipboard with ID: " + id1);
            System.out.flush();

            // Step 2: Get all clipboards
            System.out.println("\nRetrieving all clipboards:");
            getAllClipboards();
            System.out.flush();

            // Step 3: Get a specific clipboard by ID
            System.out.println("\nRetrieving clipboard with ID: " + id1);
            getClipboardById(id1);
            System.out.flush();

            // Step 4: Update a clipboard
            System.out.println("\nUpdating clipboard with ID: " + id1);
            updateClipboard(id1, "This content has been updated at " + System.currentTimeMillis());
            System.out.flush();

            // Step 5: Delete a clipboard
            System.out.println("\nDeleting clipboard with ID: " + id1);
            deleteClipboard(id1);
            System.out.flush();

        } catch (Exception e) {
            System.err.println("Error during API demo: " + e.getMessage());
            e.printStackTrace();
            System.err.flush();
        }

        System.out.println("\nClipboard API Demo completed");
        System.out.flush();
    }

    // Create a new clipboard and return its ID
    private static String createClipboard(String content) throws Exception {
        String requestBody = String.format("{\"content\": \"%s\"}", content);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + API_PATH))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status: " + response.statusCode());
        System.out.println("Response: " + response.body());

        if (response.statusCode() == 201) {
            // Parse the ID from the response
            String responseBody = response.body();
            int idStart = responseBody.indexOf("\"id\":\"") + 6;
            int idEnd = responseBody.indexOf("\"", idStart);
            return responseBody.substring(idStart, idEnd);
        } else {
            throw new RuntimeException("Failed to create clipboard. Status: " + response.statusCode() +
                    ", Response: " + response.body());
        }
    }



    // Get all clipboards
    private static void getAllClipboards() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + API_PATH))
                .header("Accept", "application/json")
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Status: " + response.statusCode());
        System.out.println("Response: " + response.body());
    }
    
    // Get a clipboard by ID
    private static void getClipboardById(String id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + API_PATH + "/" + id))
                .header("Accept", "application/json")
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Status: " + response.statusCode());
        System.out.println("Response: " + response.body());
    }
    
    // Update a clipboard
    private static void updateClipboard(String id, String newContent) throws Exception {
        String requestBody = String.format("{\"id\": \"%s\", \"content\": \"%s\"}", id, newContent);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + API_PATH + "/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Status: " + response.statusCode());
        System.out.println("Response: " + response.body());
    }
    
    // Delete a clipboard
    private static void deleteClipboard(String id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + API_PATH + "/" + id))
                .DELETE()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Status: " + response.statusCode());
        if (!response.body().isEmpty()) {
            System.out.println("Response: " + response.body());
        } else {
            System.out.println("No response body (as expected for DELETE)");
        }
    }
}