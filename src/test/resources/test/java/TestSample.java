package io.joshuasalcedo.test;

import java.util.List;
import java.util.ArrayList;

/**
 * This is a sample Java class for testing the JavaParser.
 * It contains various elements like methods, JavaDocs, and comments
 * that can be parsed and analyzed.
 *
 * @author Joshua Salcedo
 * @version 1.0
 * @since 2023-05-01
 */
public class TestSample {

    // Class-level fields
    private String name;
    private int count;

    /**
     * Default constructor.
     */
    public TestSample() {
        this.name = "default";
        this.count = 0;
    }

    /**
     * Parameterized constructor.
     *
     * @param name The name to set
     * @param count The count to set
     */
    public TestSample(String name, int count) {
        this.name = name;
        this.count = count;
    }

    /**
     * Returns the name field.
     *
     * @return The name field
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name field.
     *
     * @param name The new name value
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the count field.
     *
     * @return The count field
     */
    public int getCount() {
        return this.count;
    }

    /**
     * Sets the count field.
     *
     * @param count The new count value
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Processes a list of strings and returns a filtered list.
     *
     * @param items The list of strings to process
     * @param filter The filter string to apply
     * @return A filtered list of strings
     * @throws IllegalArgumentException If items is null
     */
    public List<String> processItems(List<String> items, String filter) throws IllegalArgumentException {
        if (items == null) {
            throw new IllegalArgumentException("Items list cannot be null");
        }

        List<String> result = new ArrayList<>();

        // Filter items based on the provided filter
        for (String item : items) {
            if (filter == null || item.contains(filter)) {
                result.add(item);
            }
        }

        return result;
    }

    /**
     * Static utility method to calculate the sum of an array of integers.
     *
     * @param numbers The array of integers to sum
     * @return The sum of all numbers in the array
     */
    public static int calculateSum(int[] numbers) {
        int sum = 0;

        // Simple summation
        for (int num : numbers) {
            sum += num;
        }

        return sum;
    }

    // Inner class for demonstration purposes
    private class InnerHelper {
        public void helperMethod() {
            // This is a helper method in an inner class
            System.out.println("Helper method called");
        }
    }
}