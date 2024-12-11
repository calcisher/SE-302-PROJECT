package ClassAssignmentSystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class MainViewController {

    // File References
    private File coursesCsvFile = null;
    private File classroomsCsvFile = null;
    private DatabaseManager dbManager = new DatabaseManager("university.db");
    private VBox studentDetailsPane;
    // UI Components - Buttons
    @FXML
    private Button btnSelectCoursesCSV;

    @FXML
    private Button btnAssignCourses;

    @FXML
    private Button btnSelectClassroomsCSV;

    @FXML
    private Button btnImport;

    @FXML
    private Button btnDelete;

    @FXML
    private Button deleteStudent;

    @FXML
    private Button btnListCourses;

    @FXML
    private Button btnListClassrooms;

    @FXML
    private Button btnListStudents;

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

        // Initially disable "Assign Courses" button
        btnAssignCourses.setDisable(true); // Initially disable

        // Enable enhanced assignment after import
        btnImport.setOnAction(event -> {
            handleImport();
            btnAssignCourses.setDisable(false);
        });

        deleteStudent.setDisable(true); // Initially hidden

        try {
            // Add double-click listener for the table rows
            studentsListView.setCellFactory(tv -> {
                ListCell<String> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null); // Clear text for empty cells
                        } else {
                            setText(item); // Set text for non-empty cells
                        }
                    }
                };

                cell.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 1 && !cell.isEmpty()) {
                        String selectedStudent = studentsListView.getSelectionModel().getSelectedItem();
                        String courseCode=coursesListView.getSelectionModel().getSelectedItem();
                        if (selectedStudent != null) {
                            deleteStudent.setDisable(false); // Show button when a student is selected
                            deleteStudent.setOnAction(e -> deleteStudentFromCourse(courseCode, selectedStudent));
                        }
                    }
                });

                return cell;
            });
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to retrieve classrooms.");
        }



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
                CSVImporter.importClassroomData(classroomsCsvFile,dbManager);
                CSVImporter.importCourseData(coursesCsvFile,dbManager);
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
                    studentsListView.setOnMouseClicked(event -> {
                        String selectedStudent = studentsListView.getSelectionModel().getSelectedItem();
                        if (selectedStudent != null) {
                            // Create the "Delete" button dynamically
                            deleteStudent = new Button("Delete");
                            deleteStudent.setOnAction(deleteEvent -> {
                                deleteStudentFromCourse(course.getCode(), selectedStudent);
                            });
                            studentDetailsPane.getChildren().clear();
                            studentDetailsPane.getChildren().add(deleteStudent);
                        }
                    });

                } else {
                    showAlert(Alert.AlertType.WARNING, "No Data", "No details found for the selected course.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to retrieve course details.");
            }
        }
    }
    @FXML
    private void handleDeleteStudent() {
        String selectedStudent = studentsListView.getSelectionModel().getSelectedItem();
        String selectedCourseCode=coursesListView.getSelectionModel().getSelectedItem();
        if (selectedStudent != null) {
            // Call the delete method and update the UI
            deleteStudentFromCourse(selectedCourseCode, selectedStudent);
        }
    }
    private void deleteStudentFromCourse(String courseCode, String studentName) {
        try {
            // Confirm deletion
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText("Are you sure you want to delete the student?");
            confirmationAlert.setContentText("Student: " + studentName);

            // Handle user response
            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Remove the student from the course in the database
                boolean success = dbManager.removeStudentFromCourse(courseCode, studentName);
                if (success) {
                    // Update the ListView
                    studentsListView.getItems().remove(studentName);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Student removed from the course successfully.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to remove student from the course.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while removing the student.");
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

    @FXML
    private void handleListStudents(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("StudentListUI.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Student List");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
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
