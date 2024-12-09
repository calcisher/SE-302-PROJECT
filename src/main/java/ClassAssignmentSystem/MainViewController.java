package ClassAssignmentSystem;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class MainViewController {

    // File References
    private File coursesCsvFile = null;
    private File classroomsCsvFile = null;
    private final DatabaseManager dbManager = new DatabaseManager("university.db");

    // UI Components - Buttons
    @FXML
    private Button btnSelectCoursesCSV;

    @FXML
    private Button btnAssignCourses;

    @FXML
    private Button btnSelectClassroomsCSV;

    @FXML
    private Button btnStudentWeeklySchedule;

    @FXML
    private Button btnClassroomWeeklySchedule;

    @FXML
    private Button btnImport;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnListCourses;

    @FXML
    private Button btnListClassrooms;


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
        // Disable Import button until both CSV files are selected
        updateImportButtonState();

        // Add listeners to list views
        coursesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> displayCourseDetails(newValue));
        classroomsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> displayClassroomDetails(newValue));
        studentsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            // Enable the btnStudentWeeklySchedule if a student is selected
            btnStudentWeeklySchedule.setDisable(newVal == null || newVal.trim().isEmpty());
        });

        classroomsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            // Enable the btnClassroomWeeklySchedule if a classroom is selected
            btnClassroomWeeklySchedule.setDisable(newVal == null || newVal.trim().isEmpty());
        });

        // Initially disable "Assign Courses" button
        btnAssignCourses.setDisable(true); // Initially disable

        // Enable enhanced assignment after import
        btnImport.setOnAction(event -> {
            handleImport();
            btnAssignCourses.setDisable(false);
        });
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

    @FXML
    private void handleAssignCourses() {
        try {
            boolean success = dbManager.assignCoursesToClassrooms();
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Assignment Successful", "All courses have been assigned to classrooms successfully.");
                handleListCourses(); // Refresh the courses list to show assigned classrooms
            } else {
                showAlert(Alert.AlertType.WARNING, "Partial Assignment", "Some courses could not be assigned to any classroom due to capacity or time conflicts.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred during assignment.");
        }
    }


    // Handler for Assign Courses button
    @FXML
    private void handleAssignCoursesEnhanced() {
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
        try {
            CSVImporter.importClassroomData(classroomsCsvFile, dbManager);
            CSVImporter.importCourseData(coursesCsvFile, dbManager);
            dbManager.addClassroomColumnIfMissing();
            showAlert(Alert.AlertType.INFORMATION, "Import Successful", "Courses and Classrooms imported successfully.");
            btnAssignCourses.setDisable(false); //Enable Assign Courses button after import
            btnDelete.setDisable(false); //Enable delete data button after import

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Import Failed", "There was an error importing the data.");
        }
    }

    // Handler for Delete button
    @FXML
    private void handleDelete() {
        try {
            dbManager.deleteDatabase();

            showAlert(Alert.AlertType.INFORMATION, "Delete Successful", "Database Deleted successfully.");
            btnDelete.setDisable(true);// disable delete data button after delete.
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Delete Failed", "There was an error Deleting the data.");
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


    // Display course details and associated students
    private void displayCourseDetails(String courseName) {
        if (courseName != null) {
            try {
                Course course = dbManager.getCourseDetails(courseName);
                if (course != null) {
                    // Debugging Statements
                    System.out.println("Displaying details for course: " + course.getCode());
                    System.out.println("Assigned Classroom: " +
                            (course.getAssignedClassroom() != null ? course.getAssignedClassroom().getName() : "Not Assigned"));

                    // Update Labels
                    lblCourseID.setText("Course Code: " + course.getCode());
                    lblTimeToStart.setText("Time to Start: " + course.getTimeToStart());
                    lblDuration.setText("Duration (Hours): " + course.getDurationInLectureHours());
                    lblLecturer.setText("Lecturer: " + course.getLecturer());

                    // Correct Label Assignment for Assigned Classroom
                    if (course.getAssignedClassroom() != null) {
                        lblAssignedClassroom.setText("Assigned Classroom: " + course.getAssignedClassroom().getName());
                        lblAssignedClassroom.setTooltip(new Tooltip(course.getAssignedClassroom().getName()));
                    } else {
                        lblAssignedClassroom.setText("Assigned Classroom: Not Assigned");
                        lblAssignedClassroom.setTooltip(new Tooltip("No classroom assigned."));
                    }

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

    // Handler for showing student's weekly schedule
    @FXML
    private void handleShowStudentWeeklySchedule() {
        String selectedStudent = studentsListView.getSelectionModel().getSelectedItem();
        if (selectedStudent == null || selectedStudent.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No student selected", "Please select a student.");
            return;
        }

        try {
            List<Course> courses = dbManager.getCoursesForStudent(selectedStudent);
            displayWeeklySchedule(courses, "Weekly Schedule for " + selectedStudent);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to retrieve student's weekly schedule.");
        }
    }

    // Handler for showing classroom's weekly schedule
    @FXML
    private void handleShowClassroomWeeklySchedule() {
        String selectedClassroom = classroomsListView.getSelectionModel().getSelectedItem();
        if (selectedClassroom == null || selectedClassroom.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No classroom selected", "Please select a classroom.");
            return;
        }

        try {
            List<Course> courses = dbManager.getCoursesForClassroom(selectedClassroom);
            displayWeeklySchedule(courses, "Weekly Schedule for Classroom " + selectedClassroom);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to retrieve classroom's weekly schedule.");
        }
    }

    // Method to display the weekly schedule in a new window
    private void displayWeeklySchedule(List<Course> courses, String title) {
        if (courses == null || courses.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Schedule", "No courses found to display.");
            return;
        }

        // Create a new Stage
        Stage stage = new Stage();
        stage.setTitle(title);

        // Example days and times
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        // Define time slots: these should match your course start times from CSV
        // For demonstration: these are typical lecture start times.
        String[] times = {"8:30", "9:25", "10:20", "11:15", "12:10", "13:05", "14:00", "14:55", "15:50", "16:45", "17:40", "18:35", "19:30", "20.25", "21.20", "22.15"};


        GridPane grid = new GridPane();
        grid.setGridLinesVisible(true);
        grid.setHgap(5);
        grid.setVgap(5);

        // Column headers (Days)
        for (int i = 0; i < days.length; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.setStyle("-fx-font-weight: bold;");
            grid.add(dayLabel, i + 1, 0);
        }

        // Row headers (Times)
        for (int j = 0; j < times.length; j++) {
            Label timeLabel = new Label(times[j]);
            timeLabel.setStyle("-fx-font-weight: bold;");
            grid.add(timeLabel, 0, j + 1);
        }

        // Place courses into the grid
        for (Course c : courses) {
            // Parse the day and time from course.getTimeToStart() -> format: "Monday 8:30"
            //String[] parts = c.getTimeToStart().split(" ");
            String[] parts = c.getTimeToStart().split(" ");
            if (parts.length == 2) {
                String dayStr = parts[0];
                String startTimeStr = parts[1];

                int dayIndex = -1;
                for (int i = 0; i < days.length; i++) {
                    if (days[i].equalsIgnoreCase(dayStr)) {
                        dayIndex = i;
                        break;
                    }
                }

                int timeIndex = -1;
                for (int j = 0; j < times.length; j++) {
                    if (times[j].equals(startTimeStr)) {
                        timeIndex = j;
                        break;
                    }
                }

                if (dayIndex >= 0 && timeIndex >= 0) {
                    Label courseLabel = new Label(c.getCode() + "\n" +
                            (c.getAssignedClassroom() != null ? c.getAssignedClassroom().getName() : "") + "\n" +
                            c.getLecturer());
                    courseLabel.setStyle("-fx-background-color: #aee1f9; -fx-padding: 5;");

                    // Span multiple rows based on the duration
                    int rowSpan = c.getDurationInLectureHours();

                    grid.add(courseLabel, dayIndex + 1, timeIndex + 1, 1, rowSpan);
                } else {
                    System.out.println("Could not place course: " + c.getCode() + " because day/time not found. dayStr=" + dayStr + " startTimeStr=" + startTimeStr);
                }
            }

            ScrollPane scrollPane = new ScrollPane(grid);
            Scene scene = new Scene(scrollPane, 700, 500);
            stage.setScene(scene);
            stage.show();
        }
    }
}
