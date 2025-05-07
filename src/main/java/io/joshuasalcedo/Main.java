package io.joshuasalcedo;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // First, initialize and start the clipboard service in background
        ClipboardService clipboardService = new ClipboardService();
        clipboardService.start();
        
        System.out.println("Clipboard service started in background");

        // Then, start the KeyMappingDisplay in the EDT
        SwingUtilities.invokeLater(() -> {
            try {
                KeyMappingDisplay display = new KeyMappingDisplay();
                display.setVisible(true);
                System.out.println("Key Mapping Display UI started");
            } catch (Exception e) {
                System.err.println("Error starting key mapping display: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
