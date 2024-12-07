package ClassAssignmentSystem;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class MainViewController {

    // File References
    private File coursesCsvFile = null;
    private File classroomsCsvFile = null;
    private final DatabaseManager dbManager = new DatabaseManager("university1.db");

    // UI Components - Buttons
    @FXML
    private Button btnSelectCoursesCSV;

    @FXML
    private Button btnSelectClassroomsCSV;

    @FXML
    private Button btnImport;

    @FXML
    private Button btnListCourses;

    @FXML
    private Button btnListClassrooms;

    @FXML
    private Button btnAssignCourses;

    // UI Components - ListViews
    @FXML
    private ListView<String> coursesListView;

    @FXML
    private ListView<String> classroomsListView;

    @FXML
    private ListView<String> studentsListView;

    // UI Components - Labels for Course Details
    @FXML
    private Label lblCourseID;

    @FXML
    private Label lblTimeToStart;

    @FXML
    private Label lblDuration;

    @FXML
    private Label lblLecturer;

    @FXML
    private Label lblAssignedClassroom;

    // UI Components - Label for Classroom Details
    @FXML
    private Label lblClassroomCapacity;

    // Initialization method
    @FXML
    public void initialize() {
        // Create necessary tables if they don't exist
        try {
            dbManager.createTable("Courses", new String[]{"Course", "TimeToStart", "DurationInLectureHours", "Lecturer", "Students"});
            dbManager.createTable("Classrooms", new String[]{"Classroom", "Capacity"});
            //dbManager.createTable("Students", new String[]{"StudentID", "StudentName"});
            //dbManager.createTable("CourseAssignments", new String[]{"CourseID", "ClassroomID"});
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Initialization Error", "Failed to create necessary tables.");
        }

        // Disable Import button until both CSV files are selected
        updateImportButtonState();

        // Add listeners to list views
        coursesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> displayCourseDetails(newValue));
        classroomsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> displayClassroomDetails(newValue));

        // Initially disable "Assign Courses" button
        btnAssignCourses.setDisable(true);
    }

    // Handler for selecting Courses CSV file
    @FXML
    private void handleSelectCoursesCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Courses CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        Stage stage = (Stage) btnSelectCoursesCSV.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            coursesCsvFile = selectedFile;
            btnSelectCoursesCSV.setText("Courses CSV: " + selectedFile.getName());
        }
        updateImportButtonState();
    }

    // Handler for selecting Classrooms CSV file
    @FXML
    private void handleSelectClassroomsCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Classrooms CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        Stage stage = (Stage) btnSelectClassroomsCSV.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            classroomsCsvFile = selectedFile;
            btnSelectClassroomsCSV.setText("Classrooms CSV: " + selectedFile.getName());
        }
        updateImportButtonState();
    }

    // Enable Import button if both CSV files are selected
    private void updateImportButtonState() {
        btnImport.setDisable(!(coursesCsvFile != null && classroomsCsvFile != null));
    }

    // Handler for Import button
    @FXML
    private void handleImport() {
        if (coursesCsvFile != null && classroomsCsvFile != null) {
            try {
                // Import Courses
                List<String[]> courseData = CSVImporter.readCSV(coursesCsvFile);
                if (!courseData.isEmpty()) {
                    String[] courseColumns = courseData.getFirst();
                    dbManager.createTable("Courses", courseColumns);
                    dbManager.insertCourseData("Courses", courseColumns, courseData.subList(1, courseData.size()));
                }

                // Import Classrooms
                List<String[]> classroomData = CSVImporter.readCSV(classroomsCsvFile);
                if (!classroomData.isEmpty()) {
                    String[] classroomColumns = classroomData.getFirst();
                    dbManager.createTable("Classrooms", classroomColumns);
                    dbManager.insertClassroomData("Classrooms", classroomColumns, classroomData.subList(1, classroomData.size()));
                }

                showAlert(Alert.AlertType.INFORMATION, "Import Successful", "Courses and Classrooms imported successfully.");
                btnAssignCourses.setDisable(false); // Enable Assign Courses button after import
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Import Failed", "There was an error importing the data.");
            }
        }
    }

    // Handler for List Courses button
    @FXML
    private void handleListCourses() {
        try {
            List<String> courses = dbManager.getAllCourses();
            coursesListView.getItems().clear();
            coursesListView.getItems().addAll(courses);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to retrieve courses.");
        }
    }

    // Handler for List Classrooms button
    @FXML
    private void handleListClassrooms() {
        try {
            List<String> classrooms = dbManager.getAllClassrooms();
            classroomsListView.getItems().clear();
            classroomsListView.getItems().addAll(classrooms);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to retrieve classrooms.");
        }
    }

    // Handler for Assign Courses button
    @FXML
    private void handleAssignCourses() {
        try {
            boolean success = dbManager.assignCoursesToClassrooms();
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Assignment Successful", "Courses have been assigned to classrooms.");
                handleListCourses(); // Refresh the courses list to show assigned classrooms
            } else {
                showAlert(Alert.AlertType.ERROR, "Assignment Failed", "There was an error assigning courses to classrooms.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred during assignment.");
        }
    }

    // Display course details and associated students
    private void displayCourseDetails(String courseName) {
        if (courseName != null) {
            try {
                Course course = dbManager.getCourseDetails(courseName);
                if (course != null) {
                    lblCourseID.setText("Course Code: " + course.getCode());
                    lblTimeToStart.setText("Time to Start: " + course.getTimeToStart());
                    lblDuration.setText("Duration (Hours): " + course.getDurationInLectureHours());
                    lblLecturer.setText("Lecturer: " + course.getLecturer());
                    lblAssignedClassroom.setText("Assigned Classroom: " + (course.getAssignedClassroom() != null ? course.getAssignedClassroom() : "Not Assigned"));

                    // Populate students list
                    List<String> students = dbManager.getStudentsForCourse(course.getCode());
                    studentsListView.getItems().clear();
                    studentsListView.getItems().addAll(students);
                } else {
                    showAlert(Alert.AlertType.WARNING, "No Data", "No details found for the selected course.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to retrieve course details.");
            }
        }
    }

    // Display classroom details
    private void displayClassroomDetails(String classroomName) {
        if (classroomName != null) {
            try {
                Classroom classroom = dbManager.getClassroomDetails(classroomName);
                if (classroom != null) {
                    lblClassroomCapacity.setText("Capacity: " + classroom.getCapacity());
                } else {
                    showAlert(Alert.AlertType.WARNING, "No Data", "No details found for the selected classroom.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to retrieve classroom details.");
            }
        }
    }

    // Utility method to show alerts
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
