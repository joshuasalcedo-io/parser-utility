package io.joshuasalcedo;

import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

public class ClipboardMonitor implements ClipboardOwner {
    private final Clipboard clipboard;
    private String lastClipboardContent = "";
    private final Timer timer;
    private static final int CHECK_INTERVAL_MS = 500; // Check every 500ms
    private final HttpClient httpClient;

    // Updated to use plural "clipboards" to match your API
    private static final String PRIMARY_API_URL = "https://server.joshuasalcedo.io/api/clipboards";
    private static final String FALLBACK_API_URL = "http://localhost:8085/api/clipboards";
    private static final int SERVER_CHECK_TIMEOUT_SECONDS = 5;
    private String currentApiUrl;

    public ClipboardMonitor() {
        // Get the system clipboard
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        // Initialize HTTP client
        httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // Set the initial API URL and check availability
        currentApiUrl = PRIMARY_API_URL;
        checkServerAvailability();

        // Take initial ownership of the clipboard
        try {
            Transferable contents = clipboard.getContents(this);
            clipboard.setContents(contents, this);

            // Store initial clipboard content if it's text
            if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                lastClipboardContent = (String) contents.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        // Set up a timer to periodically check clipboard content
        timer = new Timer(true); // Run as daemon thread
        timer.scheduleAtFixedRate(new ClipboardCheckTask(), CHECK_INTERVAL_MS, CHECK_INTERVAL_MS);

        System.out.println("Clipboard monitor started. Waiting for clipboard changes...");
    }

    private void checkServerAvailability() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    // Health endpoint might still be at /health or could be at /api/clipboards/health
                    .uri(URI.create(PRIMARY_API_URL + "/health"))
                    .timeout(Duration.ofSeconds(SERVER_CHECK_TIMEOUT_SECONDS))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && response.body().contains("ok")) {
                currentApiUrl = PRIMARY_API_URL;
                System.out.println("Using primary API server");
            } else {
                currentApiUrl = FALLBACK_API_URL;
                System.out.println("Primary server not responding, using fallback API server");
            }
        } catch (Exception e) {
            System.err.println("Server check failed, defaulting to fallback server: " + e.getMessage());
            currentApiUrl = FALLBACK_API_URL;
        }
    }

    private class ClipboardCheckTask extends TimerTask {
        @Override
        public void run() {
            checkClipboard();
        }
    }

    private void checkClipboard() {
        try {
            // Get current clipboard contents
            Transferable contents = clipboard.getContents(this);

            // Check if it contains text data
            if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String currentContent = (String) contents.getTransferData(DataFlavor.stringFlavor);

                // If content has changed, print it and send to server
                if (!currentContent.equals(lastClipboardContent)) {
                    System.out.println("Clipboard changed: " + currentContent);
                    lastClipboardContent = currentContent;

                    // Send clipboard content to server
                    sendClipboardToServer(currentContent);

                    // Take ownership again to ensure we're notified of changes
                    clipboard.setContents(contents, this);
                }
            }
        } catch (UnsupportedFlavorException | IOException e) {
            System.err.println("Error checking clipboard: " + e.getMessage());
        } catch (IllegalStateException e) {
            // Clipboard might be unavailable temporarily
            System.err.println("Clipboard temporarily unavailable: " + e.getMessage());
        }
    }

    private void sendClipboardToServer(String content) {
        try {
            // Create a JSON structure for the clipboard content
            String jsonPayload = String.format("{\"content\": \"%s\"}",
                    content.replace("\"", "\\\"")
                            .replace("\n", "\\n")
                            .replace("\r", "\\r")
                            .replace("\t", "\\t"));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(currentApiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        System.out.println("Server status: " + response.statusCode());
                        return response.body();
                    })
                    .thenAccept(response -> System.out.println("Server response: " + response))
                    .exceptionally(ex -> {
                        System.err.println("Error sending clipboard content to server: " + ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            System.err.println("Failed to send clipboard content: " + e.getMessage());
        }
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        try {
            // Small delay to allow clipboard content to change
            Thread.sleep(200);

            // Get new clipboard contents
            Transferable newContents = clipboard.getContents(this);

            // Print the content if it's text and send to server
            if (newContents != null && newContents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String data = (String) newContents.getTransferData(DataFlavor.stringFlavor);

                // Only process if content has changed
                if (!data.equals(lastClipboardContent) && !data.isEmpty()) {
                    System.out.println("Clipboard changed (lost ownership): " + data);
                    lastClipboardContent = data;

                    // Send clipboard content to server
                    sendClipboardToServer(data);
                }
            }

            // Take ownership again to continue monitoring
            clipboard.setContents(newContents, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ClipboardMonitor();

        // Need to keep the program running
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}