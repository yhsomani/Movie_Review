
// Copyright (c) 2025. Created By Yash Somani
import java.util.Scanner;

// Utility class for handling and validating user input from the console
public class InputHandler {
    // Scanner instance to read input from the console
    private final Scanner scanner;

    // Constructor that initializes the Scanner for console input
    public InputHandler() {
        this.scanner = new Scanner(System.in); // Create a new Scanner to read from System.in
    }

    // Retrieves a string input from the user with a given prompt
    // Returns an empty string if the input is blank after trimming
    public String getString(String prompt) {
        System.out.print(prompt); // Display the prompt to the user
        String input = scanner.nextLine().trim(); // Read the input line and remove leading/trailing whitespace
        return input.isEmpty() ? "" : input; // Return empty string if input is empty, otherwise return the input
    }

    // Retrieves an integer input from the user with a given prompt
    // Continuously prompts until a valid integer is provided
    public int getInt(String prompt) {
        while (true) { // Loop until a valid integer is received
            System.out.print(prompt); // Display the prompt to the user
            String input = scanner.nextLine().trim(); // Read the input line and remove whitespace
            try {
                return Integer.parseInt(input); // Attempt to parse the input as an integer and return it
            } catch (NumberFormatException e) {
                // Handle invalid input (e.g., non-integer values)
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    // Retrieves an integer input within a specified range (inclusive)
    // Continuously prompts until a valid integer within the range is provided
    public int getIntInRange(String prompt, int min, int max) {
        // Validate that the minimum value is not greater than the maximum
        if (min > max) {
            throw new IllegalArgumentException("Minimum value cannot be greater than maximum value.");
        }
        while (true) { // Loop until a valid integer within the range is received
            int value = getInt(prompt); // Get an integer using the getInt method
            if (value >= min && value <= max) { // Check if the value is within the specified range
                return value; // Return the valid integer
            }
            // Inform the user of the valid range and prompt again
            System.out.printf("Please enter a number between %d and %d.%n", min, max);
        }
    }

    // Closes the Scanner to release system resources
    public void close() {
        if (scanner != null) { // Check if the scanner is not null to avoid NullPointerException
            scanner.close(); // Close the scanner
        }
    }
}