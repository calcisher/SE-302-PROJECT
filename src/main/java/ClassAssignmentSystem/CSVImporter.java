package ClassAssignmentSystem;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.*;
import java.util.*;

public class CSVImporter extends Application {

    private File coursesCsvFile = null;
    private File classroomsCsvFile = null;
    private DatabaseManager dbManager;

    // UI Components
    private Button btnSelectCoursesCSV;
    private Button btnSelectClassroomsCSV;
    private Button btnImport;
    private Button btnListCourses;
    private Button btnListClassrooms;
    private Button btnAssignCourses;
    private ListView<String> coursesListView;
    private ListView<String> classroomsListView;
    private ListView<String> studentsListView;
    private Label lblCourses;
    private Label lblClassrooms;
    private Label lblStudents;

    // Labels for Course Details
    private Label lblCourseID;
    private Label lblTimeToStart;
    private Label lblDuration;
    private Label lblLecturer;
    private Label lblAssignedClassroom;

    // Label for Classroom Details
    private Label lblClassroomCapacity;

    @Override
    public void start(Stage primaryStage) {
        dbManager = new DatabaseManager("university2.db");
        dbManager.createNormalizedTables(); // Create normalized tables

        // Initialize UI Components
        btnSelectCoursesCSV = new Button("Select Courses CSV");
        btnSelectClassroomsCSV = new Button("Select Classrooms CSV");
        btnImport = new Button("Import");
        btnImport.setDisable(true); // Disabled until both files are selected
        btnListCourses = new Button("List Courses");
        btnListClassrooms = new Button("List Classrooms");
        btnAssignCourses = new Button("Assign Courses");
        btnAssignCourses.setDisable(true); // Disabled until assignments can be made

        coursesListView = new ListView<>();
        classroomsListView = new ListView<>();
        studentsListView = new ListView<>();
        lblCourses = new Label("Courses:");
        lblClassrooms = new Label("Classrooms:");
        lblStudents = new Label("Students:");

        // Initialize Course Details Labels
        lblCourseID = new Label("Course ID: ");
        lblTimeToStart = new Label("Time to Start: ");
        lblDuration = new Label("Duration (Hours): ");
        lblLecturer = new Label("Lecturer: ");
        lblAssignedClassroom = new Label("Assigned Classroom: ");

        // Initialize Classroom Details Label
        lblClassroomCapacity = new Label("Capacity: ");

        // Set Button Actions
        btnSelectCoursesCSV.setOnAction(e -> selectCoursesCSVFile(primaryStage));
        btnSelectClassroomsCSV.setOnAction(e -> selectClassroomsCSVFile(primaryStage));
        btnImport.setOnAction(e -> importData());
        btnListCourses.setOnAction(e -> listCourses());
        btnListClassrooms.setOnAction(e -> listClassrooms());
        btnAssignCourses.setOnAction(e -> assignCourses());

        // Layout Setup

        // Selection Section
        VBox selectionSection = new VBox(10, btnSelectCoursesCSV, btnSelectClassroomsCSV, btnImport);
        selectionSection.setPadding(new Insets(10));
        selectionSection.setStyle("-fx-border-color: black; -fx-border-width: 1;");
        selectionSection.setPrefWidth(200);

        // Courses Section
        VBox coursesSection = new VBox(5, lblCourses, coursesListView);
        coursesSection.setPadding(new Insets(10));
        coursesSection.setStyle("-fx-border-color: black; -fx-border-width: 1;");
        coursesSection.setPrefWidth(300);

        // Course Details Section
        VBox courseDetailsSection = new VBox(5, lblCourseID, lblTimeToStart, lblDuration, lblLecturer, lblAssignedClassroom);
        courseDetailsSection.setPadding(new Insets(10));
        courseDetailsSection.setStyle("-fx-border-color: black; -fx-border-width: 1;");
        courseDetailsSection.setPrefWidth(300);

        // Combine Courses and Course Details
        VBox coursesAndDetails = new VBox(10, coursesSection, courseDetailsSection);

        // Students Section
        VBox studentsSection = new VBox(5, lblStudents, studentsListView);
        studentsSection.setPadding(new Insets(10));
        studentsSection.setStyle("-fx-border-color: black; -fx-border-width: 1;");
        studentsSection.setPrefWidth(300);

        // Combine Courses and Students Sections
        HBox coursesDisplay = new HBox(20, coursesAndDetails, studentsSection);
        coursesDisplay.setPadding(new Insets(10));

        // Classrooms Section
        VBox classroomsSection = new VBox(5, lblClassrooms, classroomsListView);
        classroomsSection.setPadding(new Insets(10));
        classroomsSection.setStyle("-fx-border-color: black; -fx-border-width: 1;");
        classroomsSection.setPrefWidth(300);

        // Classroom Details Section
        VBox classroomDetailsSection = new VBox(5, lblClassroomCapacity);
        classroomDetailsSection.setPadding(new Insets(10));
        classroomDetailsSection.setStyle("-fx-border-color: black; -fx-border-width: 1;");
        classroomDetailsSection.setPrefWidth(300);

        // Combine Classrooms and Classroom Details
        VBox classroomsAndDetails = new VBox(10, classroomsSection, classroomDetailsSection);

        // Classrooms List Section
        VBox classroomsDisplay = new VBox(10, classroomsAndDetails);
        classroomsDisplay.setPadding(new Insets(10));

        // Main Display Section
        HBox mainDisplay = new HBox(20, coursesDisplay, classroomsDisplay);
        mainDisplay.setPadding(new Insets(10));

        // Buttons for Listing and Assigning
        HBox listingButtons = new HBox(20, btnListCourses, btnListClassrooms, btnAssignCourses);
        listingButtons.setPadding(new Insets(10));

        // Combine All Sections
        VBox root = new VBox(20, selectionSection, listingButtons, mainDisplay);
        root.setPadding(new Insets(20));

        // Event: When a course is selected, display its students and details
        coursesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                displayCourseDetails(newValue);
            }
        });

        // Event: When a classroom is selected, display its capacity
        classroomsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                displayClassroomDetails(newValue);
            }
        });

        // Enable "Assign Courses" button if there are unassigned courses and available classrooms
        updateAssignButtonState();

        Scene scene = new Scene(root, 1300, 800);

        primaryStage.setTitle("CSV Importer and Data Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Opens a FileChooser dialog to select the Courses CSV file.
     *
     * @param stage The primary stage.
     */
    private void selectCoursesCSVFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Courses CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            coursesCsvFile = selectedFile;
            System.out.println("Selected Courses CSV File: " + coursesCsvFile.getAbsolutePath());
            checkBothFilesSelected();
        }
    }

    /**
     * Opens a FileChooser dialog to select the Classrooms CSV file.
     *
     * @param stage The primary stage.
     */
    private void selectClassroomsCSVFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Classrooms CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            classroomsCsvFile = selectedFile;
            System.out.println("Selected Classrooms CSV File: " + classroomsCsvFile.getAbsolutePath());
            checkBothFilesSelected();
        }
    }

    /**
     * Checks if both CSV files are selected and enables the Import button if so.
     */
    private void checkBothFilesSelected() {
        if (coursesCsvFile != null && classroomsCsvFile != null) {
            btnImport.setDisable(false);
            showAlert(AlertType.INFORMATION, "Files Selected", "Both CSV files have been selected. You can proceed to import.");
        }
    }

    /**
     * Imports data from the selected CSV files into the database.
     */
    private void importData() {
        if (coursesCsvFile == null || classroomsCsvFile == null) {
            showAlert(AlertType.ERROR, "Files Not Selected", "Please select both Courses and Classrooms CSV files.");
            return;
        }

        int totalCoursesRows = 0;
        int successfulCoursesRows = 0;
        int failedCoursesRows = 0;

        int totalClassroomsRows = 0;
        int successfulClassroomsRows = 0;
        int failedClassroomsRows = 0;

        try {
            // Import Courses CSV
            List<String[]> coursesRows = new ArrayList<>();
            String[] coursesHeaders = processCSV(coursesCsvFile, coursesRows, ";"); // Assuming semicolon delimiter

            if (coursesHeaders == null || coursesHeaders.length < 4) { // Ensure at least Course, TimeToStart, Duration, Lecturer
                throw new Exception("Courses CSV file is missing required headers!");
            }

            // Process each course row
            for (String[] row : coursesRows) {
                totalCoursesRows++;
                try {
                    // Trim trailing empty columns
                    row = trimTrailingEmpty(row);

                    if (row.length < 4) {
                        System.out.println("Skipping incomplete course row " + totalCoursesRows + ": " + Arrays.toString(row));
                        failedCoursesRows++;
                        continue; // Skip incomplete rows
                    }

                    String courseID = row[0].trim();
                    String timeToStart = row[1].trim();
                    int durationInLectureHours;
                    try {
                        durationInLectureHours = Integer.parseInt(row[2].trim());
                    } catch (NumberFormatException nfe) {
                        System.err.println("Invalid duration for course " + courseID + ". Skipping row " + totalCoursesRows + ".");
                        failedCoursesRows++;
                        continue; // Skip rows with invalid duration
                    }
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

                    successfulCoursesRows++;
                } catch (Exception rowEx) {
                    System.err.println("Error processing course row " + totalCoursesRows + ": " + Arrays.toString(row));
                    rowEx.printStackTrace();
                    failedCoursesRows++;
                }
            }

            // Import Classrooms CSV
            List<String[]> classroomsRows = new ArrayList<>();
            String[] classroomsHeaders = processCSV(classroomsCsvFile, classroomsRows, ";"); // Assuming semicolon delimiter

            if (classroomsHeaders == null || classroomsHeaders.length < 2) { // Ensure at least Classroom and Capacity
                throw new Exception("Classrooms CSV file is missing required headers!");
            }

            // Process each classroom row
            for (String[] row : classroomsRows) {
                totalClassroomsRows++;
                try {
                    // Trim trailing empty columns
                    row = trimTrailingEmpty(row);

                    if (row.length < 2) {
                        System.out.println("Skipping incomplete classroom row " + totalClassroomsRows + ": " + Arrays.toString(row));
                        failedClassroomsRows++;
                        continue; // Skip incomplete rows
                    }

                    String classroomID = row[0].trim();
                    int capacity;
                    try {
                        capacity = Integer.parseInt(row[1].trim());
                    } catch (NumberFormatException nfe) {
                        System.err.println("Invalid capacity for classroom " + classroomID + ". Skipping row " + totalClassroomsRows + ".");
                        failedClassroomsRows++;
                        continue; // Skip rows with invalid capacity
                    }

                    // Insert classroom
                    dbManager.insertClassroom(classroomID, capacity);

                    successfulClassroomsRows++;
                } catch (Exception rowEx) {
                    System.err.println("Error processing classroom row " + totalClassroomsRows + ": " + Arrays.toString(row));
                    rowEx.printStackTrace();
                    failedClassroomsRows++;
                }
            }

            String successMessage = String.format("Import Completed!\n\nCourses CSV:\nTotal Rows: %d\nSuccessful: %d\nFailed: %d\n\nClassrooms CSV:\nTotal Rows: %d\nSuccessful: %d\nFailed: %d",
                    totalCoursesRows, successfulCoursesRows, failedCoursesRows,
                    totalClassroomsRows, successfulClassroomsRows, failedClassroomsRows);
            showAlert(AlertType.INFORMATION, "Import Completed", successMessage);

            // Reset selected files and disable Import button
            coursesCsvFile = null;
            classroomsCsvFile = null;
            btnImport.setDisable(true);

            // Enable Assign Courses button if possible
            updateAssignButtonState();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Import Error", "An error occurred during import: " + e.getMessage());
        }
    }

    /**
     * Lists all courses in the Courses ListView.
     */
    private void listCourses() {
        List<String> courses = dbManager.getAllCourses();
        if (courses.isEmpty()) {
            showAlert(AlertType.INFORMATION, "No Courses Found", "There are no courses available in the database.");
            return;
        }
        coursesListView.getItems().clear();
        coursesListView.getItems().addAll(courses);
        studentsListView.getItems().clear(); // Clear previous student list
        clearCourseDetails();
        showAlert(AlertType.INFORMATION, "Courses Loaded", "Courses have been loaded successfully.");
    }

    /**
     * Lists all classrooms in the Classrooms ListView.
     */
    private void listClassrooms() {
        List<String> classrooms = dbManager.getAllClassrooms();
        if (classrooms.isEmpty()) {
            showAlert(AlertType.INFORMATION, "No Classrooms Found", "There are no classrooms available in the database.");
            return;
        }
        classroomsListView.getItems().clear();
        classroomsListView.getItems().addAll(classrooms);
        lblClassroomCapacity.setText("Capacity: ");
        showAlert(AlertType.INFORMATION, "Classrooms Loaded", "Classrooms have been loaded successfully.");
    }

    /**
     * Assigns courses to classrooms either automatically or manually based on user choice.
     */
    private void assignCourses() {
        // Prompt user to choose between automatic and manual assignment
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Assign Courses");
        alert.setHeaderText("Choose Assignment Method");
        alert.setContentText("Do you want to assign courses automatically or manually?");

        ButtonType buttonTypeAuto = new ButtonType("Automatic");
        ButtonType buttonTypeManual = new ButtonType("Manual");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeAuto, buttonTypeManual, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == buttonTypeAuto) {
                performAutomaticAssignment();
            } else if (result.get() == buttonTypeManual) {
                performManualAssignment();
            }
        }
    }

    /**
     * Performs automatic assignment of courses to classrooms.
     */
    private void performAutomaticAssignment() {
        List<String> unassignedCourses = dbManager.getUnassignedCourses();
        List<String> availableClassrooms = dbManager.getAvailableClassrooms();

        if (unassignedCourses.isEmpty()) {
            showAlert(AlertType.INFORMATION, "No Unassigned Courses", "All courses have already been assigned to classrooms.");
            return;
        }

        if (availableClassrooms.isEmpty()) {
            showAlert(AlertType.INFORMATION, "No Available Classrooms", "There are no available classrooms to assign.");
            return;
        }

        // Sort courses by number of students descending
        unassignedCourses.sort((c1, c2) -> {
            int size1 = dbManager.getStudentsByCourse(c1).size();
            int size2 = dbManager.getStudentsByCourse(c2).size();
            return Integer.compare(size2, size1);
        });

        // Sort classrooms by capacity descending
        availableClassrooms.sort((cl1, cl2) -> {
            int cap1 = dbManager.getClassroomCapacity(cl1);
            int cap2 = dbManager.getClassroomCapacity(cl2);
            return Integer.compare(cap2, cap1);
        });

        int assignmentsMade = 0;
        List<String> classroomsToRemove = new ArrayList<>();

        for (String courseID : unassignedCourses) {
            int courseSize = dbManager.getStudentsByCourse(courseID).size();

            for (String classroomID : availableClassrooms) {
                int classroomCapacity = dbManager.getClassroomCapacity(classroomID);
                if (classroomCapacity >= courseSize) {
                    dbManager.assignClassroomToCourse(courseID, classroomID);
                    assignmentsMade++;
                    classroomsToRemove.add(classroomID);
                    break; // Move to the next course
                }
            }
        }

        // Remove assigned classrooms from available list
        availableClassrooms.removeAll(classroomsToRemove);

        String message = String.format("Automatic Assignment Completed!\n\nTotal Assignments Made: %d", assignmentsMade);
        showAlert(AlertType.INFORMATION, "Assignment Completed", message);

        // Refresh the Assign Courses button state
        updateAssignButtonState();
    }

    /**
     * Performs manual assignment of courses to classrooms.
     */
    private void performManualAssignment() {
        List<String> unassignedCourses = dbManager.getUnassignedCourses();
        List<String> availableClassrooms = dbManager.getAvailableClassrooms();

        if (unassignedCourses.isEmpty()) {
            showAlert(AlertType.INFORMATION, "No Unassigned Courses", "All courses have already been assigned to classrooms.");
            return;
        }

        if (availableClassrooms.isEmpty()) {
            showAlert(AlertType.INFORMATION, "No Available Classrooms", "There are no available classrooms to assign.");
            return;
        }

        // Create a dialog for each unassigned course
        for (String courseID : unassignedCourses) {
            int courseSize = dbManager.getStudentsByCourse(courseID).size();

            // Get list of suitable classrooms
            List<String> suitableClassrooms = new ArrayList<>();
            for (String classroomID : availableClassrooms) {
                int capacity = dbManager.getClassroomCapacity(classroomID);
                if (capacity >= courseSize) {
                    suitableClassrooms.add(classroomID);
                }
            }

            if (suitableClassrooms.isEmpty()) {
                showAlert(AlertType.INFORMATION, "No Suitable Classroom", "No available classrooms can accommodate course " + courseID + " with " + courseSize + " students.");
                continue;
            }

            // Create ChoiceDialog for classroom selection
            ChoiceDialog<String> dialog = new ChoiceDialog<>(suitableClassrooms.get(0), suitableClassrooms);
            dialog.setTitle("Assign Classroom");
            dialog.setHeaderText("Assign Classroom to Course");
            dialog.setContentText("Select a classroom for course " + courseID + " (" + courseSize + " students):");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String selectedClassroom = result.get();
                dbManager.assignClassroomToCourse(courseID, selectedClassroom);
                availableClassrooms.remove(selectedClassroom); // Remove assigned classroom from available list
                showAlert(AlertType.INFORMATION, "Assignment Successful", "Assigned Classroom " + selectedClassroom + " to Course " + courseID + ".");
            } else {
                // User canceled assignment for this course
                showAlert(AlertType.WARNING, "Assignment Canceled", "No classroom was assigned to Course " + courseID + ".");
            }
        }

        // Refresh the Assign Courses button state
        updateAssignButtonState();
    }

    /**
     * Displays the details of the selected course and its students.
     *
     * @param courseID The ID of the selected course.
     */
    private void displayCourseDetails(String courseID) {
        // Fetch course details
        Course course = dbManager.getCourseDetails(courseID);
        if (course != null) {
            lblCourseID.setText("Course ID: " + course.getCourseID());
            lblTimeToStart.setText("Time to Start: " + course.getTimeToStart());
            lblDuration.setText("Duration (Hours): " + course.getDurationInLectureHours());
            lblLecturer.setText("Lecturer: " + course.getLecturer());

            // Fetch assigned classroom
            String assignedClassroom = dbManager.getAssignedClassroom(courseID);
            if (assignedClassroom != null) {
                lblAssignedClassroom.setText("Assigned Classroom: " + assignedClassroom);
            } else {
                lblAssignedClassroom.setText("Assigned Classroom: Not Assigned");
            }
        } else {
            lblCourseID.setText("Course ID: ");
            lblTimeToStart.setText("Time to Start: ");
            lblDuration.setText("Duration (Hours): ");
            lblLecturer.setText("Lecturer: ");
            lblAssignedClassroom.setText("Assigned Classroom: ");
            showAlert(AlertType.ERROR, "Course Not Found", "Details for course " + courseID + " were not found.");
        }

        // Fetch and display students
        List<String> students = dbManager.getStudentsByCourse(courseID);
        if (students.isEmpty()) {
            studentsListView.getItems().clear();
            showAlert(AlertType.INFORMATION, "No Students Found", "No students are enrolled in course: " + courseID);
            return;
        }
        studentsListView.getItems().clear();
        studentsListView.getItems().addAll(students);
    }

    /**
     * Displays the capacity of the selected classroom.
     *
     * @param classroomID The ID of the selected classroom.
     */
    private void displayClassroomDetails(String classroomID) {
        int capacity = dbManager.getClassroomCapacity(classroomID);
        if (capacity != -1) {
            lblClassroomCapacity.setText("Capacity: " + capacity);
        } else {
            lblClassroomCapacity.setText("Capacity: ");
            showAlert(AlertType.ERROR, "Classroom Not Found", "Details for classroom " + classroomID + " were not found.");
        }
    }

    /**
     * Clears the course details labels.
     */
    private void clearCourseDetails() {
        lblCourseID.setText("Course ID: ");
        lblTimeToStart.setText("Time to Start: ");
        lblDuration.setText("Duration (Hours): ");
        lblLecturer.setText("Lecturer: ");
        lblAssignedClassroom.setText("Assigned Classroom: ");
    }

    /**
     * Processes the CSV file and extracts headers and data rows.
     *
     * @param csvFile   The CSV file to process.
     * @param rows      The list to populate with data rows.
     * @param delimiter The delimiter used in the CSV file.
     * @return An array of header names.
     * @throws Exception If an error occurs during processing.
     */
    private String[] processCSV(File csvFile, List<String[]> rows, String delimiter) throws Exception {
        try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(csvFile))
                .withCSVParser(new com.opencsv.CSVParserBuilder().withSeparator(delimiter.charAt(0)).build())
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

            System.out.println("Total Data Rows Found: " + rowCount);
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

    /**
     * Updates the state of the "Assign Courses" button based on available assignments.
     */
    private void updateAssignButtonState() {
        List<String> unassignedCourses = dbManager.getUnassignedCourses();
        List<String> availableClassrooms = dbManager.getAvailableClassrooms();

        if (!unassignedCourses.isEmpty() && !availableClassrooms.isEmpty()) {
            btnAssignCourses.setDisable(false);
        } else {
            btnAssignCourses.setDisable(true);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
