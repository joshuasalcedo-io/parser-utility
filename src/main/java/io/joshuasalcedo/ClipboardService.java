package io.joshuasalcedo;

import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClipboardService {
    private final Clipboard clipboard;
    private String lastClipboardContent = "";
    private static final int SAVE_INTERVAL_MS = 300000; // Save every 5 minutes (300,000ms)
    private final HttpClient httpClient;
    private static final String PRIMARY_API_URL = "https://server.joshuasalcedo.io/api/clipboard";
    private static final String FALLBACK_API_URL = "http://localhost:8085/api/clipboard";
    private static final int SERVER_CHECK_TIMEOUT_SECONDS = 5;
    private String currentApiUrl;

    // Thread control
    private final ExecutorService clipboardExecutor = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);
    private ClipboardListenerTask clipboardListenerTask;
    private ScheduledFuture<?> serverSaveTask;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public ClipboardService() {
        // Get the system clipboard
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        // Initialize HTTP client (using Java 11+ HttpClient)
        httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // Check if primary server is available, otherwise use fallback
        checkServerAvailability();

        // Add shutdown hook to clean up threads
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stopMonitoring();
            clipboardExecutor.shutdownNow();
            scheduledExecutor.shutdownNow();
        }));

        System.out.println("[ClipboardService] Initialized");
    }

    public void start() {
        if (isRunning.getAndSet(true)) {
            return; // Already running
        }

        System.out.println("[ClipboardService] Starting clipboard monitoring...");

        // Initialize the clipboard listener task
        clipboardListenerTask = new ClipboardListenerTask();
        clipboardExecutor.submit(clipboardListenerTask);

        // Schedule the server save task
        serverSaveTask = scheduledExecutor.scheduleAtFixedRate(
                this::saveClipboardHistoryToServer,
                SAVE_INTERVAL_MS,
                SAVE_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );

        System.out.println("[ClipboardService] Clipboard monitoring started");
    }

    public void stop() {
        if (!isRunning.getAndSet(false)) {
            return; // Already stopped
        }

        System.out.println("[ClipboardService] Stopping clipboard monitoring...");

        // Cancel the clipboard listener task
        if (clipboardListenerTask != null) {
            clipboardListenerTask.stop();
        }

        // Cancel the server save task
        if (serverSaveTask != null) {
            serverSaveTask.cancel(false);
        }

        System.out.println("[ClipboardService] Clipboard monitoring stopped");
    }

    private void saveClipboardHistoryToServer() {
        // This would save clipboard history to the server
        // Simplified version just logs the action
        System.out.println("[ClipboardService] Saving clipboard history to server: " + currentApiUrl);
    }

    private void checkServerAvailability() {
        CompletableFuture.supplyAsync(() -> isServerAvailable(PRIMARY_API_URL))
                .thenAccept(available -> {
                    if (available) {
                        currentApiUrl = PRIMARY_API_URL;
                        System.out.println("[ClipboardService] Using primary server: " + PRIMARY_API_URL);
                    } else {
                        currentApiUrl = FALLBACK_API_URL;
                        System.out.println("[ClipboardService] Primary server unavailable. Using fallback server: " + FALLBACK_API_URL);

                        // Schedule a task to periodically check if the primary server becomes available
                        scheduledExecutor.scheduleAtFixedRate(() -> {
                            if (isServerAvailable(PRIMARY_API_URL) &&
                                    !currentApiUrl.equals(PRIMARY_API_URL)) {
                                currentApiUrl = PRIMARY_API_URL;
                                System.out.println("[ClipboardService] Reconnected to primary server: " + PRIMARY_API_URL);
                            }
                        }, 60, 60, TimeUnit.SECONDS); // Check every minute
                    }
                });
    }

    private boolean isServerAvailable(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(SERVER_CHECK_TIMEOUT_SECONDS))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<Void> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.discarding());

            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception e) {
            return false;
        }
    }

    private CompletableFuture<String> postToServer(String url, String content) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(content))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> "Error: " + ex.getMessage());
    }

    private class ClipboardListenerTask implements Runnable {
        private final AtomicBoolean running = new AtomicBoolean(true);

        public void stop() {
            running.set(false);
        }

        @Override
        public void run() {
            try {
                // Take initial ownership of the clipboard
                Transferable contents = clipboard.getContents(null);
                ClipboardOwner owner = new ClipboardOwner() {
                    @Override
                    public void lostOwnership(Clipboard clipboard, Transferable contents) {
                        if (!running.get()) return;

                        try {
                            // Small delay to allow clipboard content to change
                            Thread.sleep(200);

                            // Get new clipboard contents
                            Transferable newContents = clipboard.getContents(null);

                            // Process the content if it's text
                            if (newContents != null && newContents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                                String data = (String) newContents.getTransferData(DataFlavor.stringFlavor);

                                // Only process if content has changed
                                if (!data.equals(lastClipboardContent) && !data.isEmpty()) {
                                    lastClipboardContent = data;
                                    System.out.println("[ClipboardService] Clipboard changed: " + data);

                                    // Send clipboard content to server
                                    postToServer(currentApiUrl, data)
                                            .thenAccept(response -> System.out.println("[ClipboardService] Server response: " + response))
                                            .exceptionally(ex -> {
                                                System.err.println("[ClipboardService] Error sending clipboard content to server: " + ex.getMessage());
                                                return null;
                                            });
                                }
                            }

                            // Take ownership again to continue monitoring
                            if (running.get()) {
                                clipboard.setContents(newContents, this);
                            }
                        } catch (Exception e) {
                            System.err.println("[ClipboardService] Error processing clipboard content: " + e.getMessage());

                            // Try to recover ownership
                            if (running.get()) {
                                try {
                                    clipboard.setContents(clipboard.getContents(null), this);
                                } catch (Exception ex) {
                                    System.err.println("[ClipboardService] Failed to recover clipboard ownership: " + ex.getMessage());
                                }
                            }
                        }
                    }
                };

                // Store initial clipboard content if it's text
                if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    lastClipboardContent = (String) contents.getTransferData(DataFlavor.stringFlavor);
                }

                // Take ownership
                clipboard.setContents(contents, owner);

                // Keep the thread alive until stopped
                while (running.get()) {
                    // Periodically check if we still have clipboard ownership
                    try {
                        Thread.sleep(1000);
                        // Sometimes ownership can be lost without notification, so we check periodically
                        if (running.get()) {
                            Transferable current = clipboard.getContents(null);
                            if (current != null && current.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                                String currentText = (String) current.getTransferData(DataFlavor.stringFlavor);
                                // If content changed but we didn't get a lostOwnership notification
                                if (!currentText.equals(lastClipboardContent)) {
                                    lastClipboardContent = currentText;
                                    System.out.println("[ClipboardService] Clipboard changed (poll): " + currentText);

                                    // Retake ownership
                                    clipboard.setContents(current, owner);

                                    // Send to server
                                    postToServer(currentApiUrl, currentText)
                                            .thenAccept(response -> System.out.println("[ClipboardService] Server response: " + response))
                                            .exceptionally(ex -> {
                                                System.err.println("[ClipboardService] Error sending clipboard content to server: " + ex.getMessage());
                                                return null;
                                            });
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (running.get()) {
                            System.err.println("[ClipboardService] Error polling clipboard: " + e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[ClipboardService] Clipboard listener error: " + e.getMessage());
            }
        }
    }
}