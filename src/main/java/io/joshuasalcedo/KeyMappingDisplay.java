package io.joshuasalcedo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyMappingDisplay extends JFrame {
    
    private final float TRANSPARENCY = 0.85f; // 85% opaque
    private final Map<String, List<ShortcutEntry>> categoryShortcuts = new HashMap<>();
    private final JPanel mainPanel;
    private final JCheckBox editModeCheckBox;
    private final JTable shortcutsTable;
    private final DefaultTableModel tableModel;
    private final JComboBox<String> categoryComboBox;
    
    private static final String CONFIG_FILE = "key_mappings.conf";

    public KeyMappingDisplay() {
        setTitle("IntelliJ Key Mappings");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setSize(450, 350);
        
        // Load initial data
        initializeShortcuts();
        
        // Create the main panel with a border
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Add control panel at the top
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Create category selector
        categoryComboBox = new JComboBox<>(categoryShortcuts.keySet().toArray(new String[0]));
        categoryComboBox.addActionListener(e -> refreshTable());
        controlPanel.add(new JLabel("Category:"));
        controlPanel.add(categoryComboBox);
        
        // Create edit mode toggle
        editModeCheckBox = new JCheckBox("Edit Mode");
        editModeCheckBox.addActionListener(e -> refreshTable());
        controlPanel.add(editModeCheckBox);
        
        // Add save button
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveShortcuts());
        controlPanel.add(saveButton);
        
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        
        // Create table for shortcuts
        String[] columnNames = {"Shortcut", "Action"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return editModeCheckBox.isSelected();
            }
        };
        
        shortcutsTable = new JTable(tableModel);
        shortcutsTable.setRowHeight(25);
        shortcutsTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        shortcutsTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        shortcutsTable.getColumnModel().getColumn(1).setPreferredWidth(280);
        
        JScrollPane scrollPane = new JScrollPane(shortcutsTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add a panel for the button at the bottom
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JLabel opacityLabel = new JLabel("Opacity:");
        bottomPanel.add(opacityLabel);
        
        // Add opacity slider
        JSlider opacitySlider = new JSlider(JSlider.HORIZONTAL, 10, 100, (int)(TRANSPARENCY * 100));
        opacitySlider.addChangeListener(e -> {
            float opacity = opacitySlider.getValue() / 100.0f;
            setOpacity(opacity);
        });
        bottomPanel.add(opacitySlider);
        
        // Add control to add new entry
        JButton addButton = new JButton("Add Entry");
        addButton.addActionListener(e -> addNewEntry());
        bottomPanel.add(addButton);
        
        // Add to main panel
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
        
        // Make window draggable
        DragWindowListener dragListener = new DragWindowListener();
        addMouseListener(dragListener);
        addMouseMotionListener(dragListener);
        
        // Refresh table with initial data
        refreshTable();
        
        // Set transparency of the window
        setOpacity(TRANSPARENCY);
        
        // Center on screen
        setLocationRelativeTo(null);
    }
    
    private void initializeShortcuts() {
        try {
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                loadFromFile();
                return;
            }
        } catch (Exception e) {
            System.err.println("Error loading config: " + e.getMessage());
        }
        
        // Default shortcuts if file doesn't exist
        List<ShortcutEntry> navigationShortcuts = new ArrayList<>();
        navigationShortcuts.add(new ShortcutEntry("ALT + 1", "Toggle Project view"));
        navigationShortcuts.add(new ShortcutEntry("ALT + 2", "Toggle Bookmarks"));
        navigationShortcuts.add(new ShortcutEntry("ALT + 3", "Toggle Structure view"));
        navigationShortcuts.add(new ShortcutEntry("ALT + 4", "Toggle Run/Debug tool window"));
        navigationShortcuts.add(new ShortcutEntry("ALT + 5", "Toggle Terminal"));
        categoryShortcuts.put("Navigation", navigationShortcuts);
        
        List<ShortcutEntry> codeOperations = new ArrayList<>();
        codeOperations.add(new ShortcutEntry("ALT + 7", "Generate Code"));
        codeOperations.add(new ShortcutEntry("ALT + 8", "Reformat Code"));
        codeOperations.add(new ShortcutEntry("ALT + 9", "Optimize Imports"));
        categoryShortcuts.put("Code Operations", codeOperations);
        
        List<ShortcutEntry> refactoring = new ArrayList<>();
        refactoring.add(new ShortcutEntry("ALT + 4", "Rename"));
        refactoring.add(new ShortcutEntry("ALT + 5", "Extract Method"));
        refactoring.add(new ShortcutEntry("ALT + 6", "Extract Variable/Constant"));
        categoryShortcuts.put("Refactoring", refactoring);
        
        List<ShortcutEntry> runDebug = new ArrayList<>();
        runDebug.add(new ShortcutEntry("ALT + Numpad 0", "Run current configuration"));
        runDebug.add(new ShortcutEntry("ALT + Numpad .", "Debug current configuration"));
        runDebug.add(new ShortcutEntry("ALT + Numpad Enter", "Run with Coverage"));
        categoryShortcuts.put("Run & Debug", runDebug);
        
        List<ShortcutEntry> versionControl = new ArrayList<>();
        versionControl.add(new ShortcutEntry("ALT + Numpad +", "Show diff"));
        versionControl.add(new ShortcutEntry("ALT + Numpad -", "Show history"));
        versionControl.add(new ShortcutEntry("ALT + Numpad *", "Commit changes"));
        categoryShortcuts.put("Version Control", versionControl);
        
        List<ShortcutEntry> codeAnalysis = new ArrayList<>();
        codeAnalysis.add(new ShortcutEntry("ALT + Numpad /", "Find Usages"));
        codeAnalysis.add(new ShortcutEntry("ALT + Numpad 1", "Go to Implementation"));
        codeAnalysis.add(new ShortcutEntry("ALT + Numpad 3", "Show quick documentation"));
        categoryShortcuts.put("Code Analysis", codeAnalysis);
    }
    
    private void refreshTable() {
        tableModel.setRowCount(0);
        
        String selectedCategory = (String) categoryComboBox.getSelectedItem();
        if (selectedCategory == null) return;
        
        List<ShortcutEntry> entries = categoryShortcuts.get(selectedCategory);
        for (ShortcutEntry entry : entries) {
            tableModel.addRow(new String[]{entry.shortcut, entry.action});
        }
    }
    
    private void addNewEntry() {
        String selectedCategory = (String) categoryComboBox.getSelectedItem();
        if (selectedCategory == null) return;
        
        List<ShortcutEntry> entries = categoryShortcuts.get(selectedCategory);
        entries.add(new ShortcutEntry("New Shortcut", "New Action"));
        refreshTable();
        
        // Set focus on the new row for editing
        int lastRow = tableModel.getRowCount() - 1;
        shortcutsTable.editCellAt(lastRow, 0);
        shortcutsTable.getEditorComponent().requestFocus();
    }
    
    private void saveShortcuts() {
        // First update the current category from the table
        updateCurrentCategoryFromTable();
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(CONFIG_FILE))) {
            for (String category : categoryShortcuts.keySet()) {
                writer.println("[" + category + "]");
                
                List<ShortcutEntry> entries = categoryShortcuts.get(category);
                for (ShortcutEntry entry : entries) {
                    writer.println(entry.shortcut + "=" + entry.action);
                }
                
                writer.println(); // Empty line between categories
            }
            JOptionPane.showMessageDialog(this, "Settings saved successfully!", 
                    "Save Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving settings: " + e.getMessage(), 
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateCurrentCategoryFromTable() {
        String selectedCategory = (String) categoryComboBox.getSelectedItem();
        if (selectedCategory == null) return;
        
        List<ShortcutEntry> entries = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String shortcut = (String) tableModel.getValueAt(i, 0);
            String action = (String) tableModel.getValueAt(i, 1);
            entries.add(new ShortcutEntry(shortcut, action));
        }
        
        categoryShortcuts.put(selectedCategory, entries);
    }
    
    private void loadFromFile() {
        categoryShortcuts.clear();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE))) {
            String line;
            String currentCategory = null;
            List<ShortcutEntry> currentEntries = null;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                if (line.startsWith("[") && line.endsWith("]")) {
                    // Save previous category if needed
                    if (currentCategory != null && currentEntries != null) {
                        categoryShortcuts.put(currentCategory, currentEntries);
                    }
                    
                    // Start new category
                    currentCategory = line.substring(1, line.length() - 1);
                    currentEntries = new ArrayList<>();
                } else if (currentCategory != null && line.contains("=")) {
                    // Parse shortcut entry
                    int separatorIndex = line.indexOf('=');
                    String shortcut = line.substring(0, separatorIndex);
                    String action = line.substring(separatorIndex + 1);
                    currentEntries.add(new ShortcutEntry(shortcut, action));
                }
            }
            
            // Save last category
            if (currentCategory != null && currentEntries != null) {
                categoryShortcuts.put(currentCategory, currentEntries);
            }
            
            // Update combo box with loaded categories
            categoryComboBox.removeAllItems();
            for (String category : categoryShortcuts.keySet()) {
                categoryComboBox.addItem(category);
            }
        } catch (IOException e) {
            System.err.println("Error reading config file: " + e.getMessage());
        }
    }
    
    private static class ShortcutEntry {
        String shortcut;
        String action;
        
        ShortcutEntry(String shortcut, String action) {
            this.shortcut = shortcut;
            this.action = action;
        }
    }
    
    private class DragWindowListener extends MouseAdapter {
        private Point dragStart;
        
        @Override
        public void mousePressed(MouseEvent e) {
            dragStart = e.getPoint();
        }
        
        @Override
        public void mouseDragged(MouseEvent e) {
            if (dragStart != null) {
                Point currentLocation = getLocation();
                setLocation(
                    currentLocation.x + e.getX() - dragStart.x,
                    currentLocation.y + e.getY() - dragStart.y
                );
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            dragStart = null;
        }
    }
    
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Enable anti-aliased text
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        SwingUtilities.invokeLater(() -> {
            KeyMappingDisplay display = new KeyMappingDisplay();
            display.setVisible(true);
        });
    }
}