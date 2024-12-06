package ClassAssignmentSystem;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import javafx.application.Application;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.*;
import java.util.*;

public class CSVImporter extends Application {

    private File csvFile = null;
    private DatabaseManager dbManager;

    @Override
    public void start(Stage primaryStage) {
        dbManager = new DatabaseManager("university5.db");
        dbManager.createNormalizedTables(); // Create normalized tables

        Button btnSelectCSV = new Button("Select CSV File");
        Button btnImport = new Button("Import");
        btnImport.setDisable(true);

        btnSelectCSV.setOnAction(e -> {
            File file = chooseCSVFile(primaryStage);
            if (file != null) {
                csvFile = file;
                btnImport.setDisable(false);
                System.out.println("Selected CSV File: " + csvFile.getAbsolutePath());
            }
        });

        btnImport.setOnAction(e -> importData());

        VBox root = new VBox(10, btnSelectCSV, btnImport);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");
        Scene scene = new Scene(root, 400, 200);

        primaryStage.setTitle("CSV Importer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Opens a FileChooser dialog to select a CSV file.
     *
     * @param stage The primary stage.
     * @return The selected CSV file or null if none selected.
     */
    private File chooseCSVFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        return fileChooser.showOpenDialog(stage);
    }

    /**
     * Imports data from the selected CSV file into the database.
     */
    private void importData() {
        if (csvFile == null) {
            showAlert(AlertType.ERROR, "No CSV File Selected", "Please select a CSV file to import.");
            return;
        }

        try {
            List<String[]> rows = new ArrayList<>();
            String[] headers = processCSV(csvFile, rows);

            if (headers == null || headers.length < 4) { // Ensure at least Course, TimeToStart, Duration, Lecturer
                throw new Exception("CSV file is missing required headers!");
            }

            // Process each row
            for (String[] row : rows) {
                // Trim trailing empty columns
                row = trimTrailingEmpty(row);

                if (row.length < 4) {
                    System.out.println("Skipping incomplete row: " + Arrays.toString(row));
                    continue; // Skip incomplete rows
                }

                String courseID = row[0].trim();
                String timeToStart = row[1].trim();
                int durationInLectureHours = Integer.parseInt(row[2].trim());
                String lecturer = row[3].trim();

                // Insert course
                dbManager.insertCourse(courseID, timeToStart, durationInLectureHours, lecturer);

                // Insert students and link to course
                for (int i = 4; i < row.length; i++) {
                    String studentName = row[i].trim();
                    if (studentName.isEmpty()) continue; // Skip empty student names

                    int studentID = dbManager.insertStudent(studentName);
                    if (studentID != -1) {
                        dbManager.insertCourseStudent(courseID, studentID);
                    }
                }
            }

            showAlert(AlertType.INFORMATION, "Import Successful", "CSV file processed and data stored in the database!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }

    /**
     * Processes the CSV file and extracts headers and data rows.
     *
     * @param csvFile The CSV file to process.
     * @param rows    The list to populate with data rows.
     * @return An array of header names.
     * @throws Exception If an error occurs during processing.
     */
    private String[] processCSV(File csvFile, List<String[]> rows) throws Exception {
        char delimiter = detectDelimiter(csvFile);
        System.out.println("Detected Delimiter: '" + delimiter + "'");

        try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(csvFile))
                .withCSVParser(new CSVParserBuilder().withSeparator(delimiter).build())
                .build()) {
            String[] headers = csvReader.readNext(); // Read the first row (headers)
            if (headers == null || headers.length == 0) {
                throw new Exception("CSV file is empty or missing headers!");
            }

            // Trim trailing empty columns from headers
            headers = trimTrailingEmpty(headers);

            System.out.println("CSV Headers:");
            for (String header : headers) {
                System.out.println(" - " + header);
            }

            String[] row;
            int rowCount = 0;
            while ((row = csvReader.readNext()) != null) {
                // Trim trailing empty columns from data rows
                row = trimTrailingEmpty(row);
                rows.add(row);
                rowCount++;
            }

            System.out.println("Total Data Rows Imported: " + rowCount);
            return headers;
        }
    }

    /**
     * Trims trailing empty strings from the array.
     *
     * @param array The original array.
     * @return A new array with trailing empty strings removed.
     */
    private String[] trimTrailingEmpty(String[] array) {
        int end = array.length;
        while (end > 0 && (array[end - 1] == null || array[end - 1].trim().isEmpty())) {
            end--;
        }
        return Arrays.copyOf(array, end);
    }

    /**
     * Detects the delimiter used in the CSV file by analyzing the first line.
     *
     * @param csvFile The CSV file to analyze.
     * @return The detected delimiter character.
     * @throws IOException If an error occurs while reading the file.
     */
    private char detectDelimiter(File csvFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String firstLine = br.readLine();
            if (firstLine == null) {
                throw new IOException("CSV file is empty!");
            }

            // Common delimiters to check
            char[] possibleDelimiters = {',', ';', '\t', '|'};
            int maxCount = 0;
            char detectedDelimiter = ',';

            for (char delimiter : possibleDelimiters) {
                int count = countOccurrences(firstLine, delimiter);
                if (count > maxCount) {
                    maxCount = count;
                    detectedDelimiter = delimiter;
                }
            }

            return detectedDelimiter;
        }
    }

    /**
     * Counts the number of occurrences of a character in a string.
     *
     * @param line      The string to search.
     * @param delimiter The character to count.
     * @return The number of occurrences.
     */
    private int countOccurrences(String line, char delimiter) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == delimiter) {
                count++;
            }
        }
        return count;
    }

    /**
     * Displays an alert dialog to the user.
     *
     * @param type    The type of alert.
     * @param title   The title of the alert.
     * @param message The message content.
     */
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
