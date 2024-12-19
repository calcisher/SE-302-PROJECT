package ClassAssignmentSystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainViewController {


    // File References
    private File coursesCsvFile = null;
    private File classroomsCsvFile = null;
    private final DatabaseManager dbManager = new DatabaseManager("university.db");
    private static MainViewController instance;
    private final Stage studentListAddstage = new Stage();

    // UI Components - Buttons
    @FXML
    private Button btnAddStudent;

    @FXML
    public Button btnCreateCourse;

    @FXML
    private Button btnHelp;

    @FXML
    private Button btnDeleteStudent;

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
    private Button btnListCourses;

    @FXML
    private Button btnListClassrooms;

    @FXML
    private Button btnListStudents;

    @FXML
    private Button btnChangeClassroom;

    // UI Components - ListViews
    @FXML
    private ListView<String> coursesListView;

    @FXML
    private ListView<String> classroomsListView;

    @FXML
    private ListView<String> studentsListView;


    // UI Components - Labels
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

    @FXML
    private Label lblClassroomCapacity;


    // Initialization method
    @FXML
    public void initialize() throws SQLException {
        // Disable Import button until both CSV files are selected
        updateImportButtonState();

        if (DatabaseManager.areTablesPresent()) {
            btnDelete.setDisable(false);
        }

        // Add listeners to list views
        coursesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> displayCourseDetails(newValue));
        classroomsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> displayClassroomDetails(newValue));
        coursesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean isSelected = newValue != null;
            btnChangeClassroom.setDisable(!isSelected);
            displayCourseDetails(newValue);
        });

        // Initially disable "Assign Courses" button
        btnAssignCourses.setDisable(true); // Initially disable

        // Enable enhanced assignment after import
        btnImport.setOnAction(event -> {
            handleImport();
            btnAssignCourses.setDisable(false);
        });
    }

    public MainViewController() {
        instance = this;
    }

    public static MainViewController getInstance() {
        return instance;
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

    // Handler for assignCourse
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

    // Handler for Assign Courses button (not used.)
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
        // Confirm Deletion with the User
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Deletion");
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setContentText("Are you sure you want to delete all data? This action cannot be undone.");

        // Show the confirmation dialog and wait for user response
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Invoke the deleteDatabase method
                    dbManager.deleteDatabase();

                    // Provide success feedback to the user
                    showAlert(Alert.AlertType.INFORMATION, "Delete Successful", "Database 'university.db' has been deleted successfully.");

                    // Reset the UI components
                    resetUIAfterDeletion();
                } catch (RuntimeException e) {
                    // Handle any errors that occurred during deletion
                    showAlert(Alert.AlertType.ERROR, "Delete Failed", e.getMessage());
                }
            }
        });
    }

    private void resetUIAfterDeletion() {
        // Disable Import and Delete buttons
        btnImport.setDisable(true);
        btnDelete.setDisable(true);
        btnAssignCourses.setDisable(true);

        // Reset the text of the CSV selection buttons
        btnSelectCoursesCSV.setText("Select Courses CSV");
        btnSelectClassroomsCSV.setText("Select Classrooms CSV");

        // Clear the ListViews
        coursesListView.getItems().clear();
        classroomsListView.getItems().clear();
        studentsListView.getItems().clear();

        // Clear the detail labels
        lblCourseID.setText("Course ID: ");
        lblTimeToStart.setText("Time to Start: ");
        lblDuration.setText("Duration (Hours): ");
        lblLecturer.setText("Lecturer: ");
        lblAssignedClassroom.setText("Assigned Classroom: ");
        lblClassroomCapacity.setText("Capacity: ");
    }

    private void openCourseSchedule(String className) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("scheduleUI.fxml"));
            Parent root = loader.load();

            ScheduleController controller = loader.getController();
            controller.loadCourseSchedule(className);

            Stage stage = new Stage();
            stage.setTitle("Schedule for class: " + className);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
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

    @FXML
    private void handleListClassrooms() {
        try {
            List<String> classrooms = dbManager.getAllClassrooms();
            classroomsListView.getItems().clear();
            classroomsListView.getItems().addAll(classrooms);

            // Add double-click listener for the table rows
            classroomsListView.setCellFactory(tv -> {
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
                    btnDeleteStudent.setDisable(false);
                    if (event.getClickCount() == 2 && !cell.isEmpty()) {
                        String selectedCourse = cell.getItem();
                        openCourseSchedule(selectedCourse);
                    }
                });

                return cell;
            });


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

                    try {
                        List<String> students = dbManager.getStudentsForCourse(course.getCode());
                        studentsListView.getItems().clear();
                        studentsListView.getItems().addAll(students);

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
                                    btnDeleteStudent.setDisable(false);
                                }
                                if (event.getClickCount() == 2 && !cell.isEmpty()) {
                                    String selectedStudent = cell.getItem();
                                    StudentListController.openStudentSchedule(selectedStudent);
                                }
                            });

                            return cell;
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to retrieve classrooms.");
                    }
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

    @FXML
    private void handleListStudents(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("StudentListUI.fxml"));
            Parent root = loader.load();

            // Retrieve the controller and call ListAllStudentsFromDatabase
            StudentListController controller = loader.getController();
            controller.listAllStudentsFromDatabase();

            Stage stage = new Stage();
            stage.setTitle("Student List");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleChangeClassroom() {
        String selectedCourseCode = coursesListView.getSelectionModel().getSelectedItem();
        if (selectedCourseCode == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a course to change its classroom.");
            return;
        }

        try {
            // Retrieve course details
            Course course = dbManager.getCourseDetails(selectedCourseCode);
            if (course == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Selected course details could not be retrieved.");
                return;
            }

            // Ensure studentCount is correctly set
            int studentCount = course.getStudentCount();
            if (studentCount == 0) {
                showAlert(Alert.AlertType.WARNING, "No Students", "The selected course has no enrolled students.");
                return;
            }

            // Fetch available classrooms based on capacity and availability
            List<Classroom> availableClassrooms = dbManager.getAvailableClassroomsForCourse(course);

            if (availableClassrooms.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No Available Classrooms", "There are no available classrooms that meet the capacity and scheduling requirements.");
                return;
            }

            // Convert classroom names to a list for the dialog
            List<String> classroomNames = availableClassrooms.stream()
                    .map(Classroom::getName)
                    .collect(Collectors.toList());

            // Show a Choice Dialog to let the user select a new classroom
            ChoiceDialog<String> dialog = new ChoiceDialog<>(classroomNames.getFirst(), classroomNames);
            dialog.setTitle("Change Classroom");
            dialog.setHeaderText("Change Classroom for Course: " + selectedCourseCode);
            dialog.setContentText("Select a new classroom:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(newClassroomName -> {
                try {
                    // Update the classroom assignment in the database
                    dbManager.updateCourseClassroom(selectedCourseCode, newClassroomName);

                    // Update in-memory schedules
                    dbManager.updateSchedulesAfterClassroomChange(course, newClassroomName);

                    // Refresh UI to reflect changes
                    displayCourseDetails(selectedCourseCode);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Classroom updated successfully.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update classroom assignment.");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred.");
        }
    }

    // Utility method to show alerts
    void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    //for Create course button
    @FXML
    private void handleCreateCourse() {
        try {
            // Load the Create Course UI
            FXMLLoader loader = new FXMLLoader(getClass().getResource("createCourseUI.fxml"));
            Parent root = loader.load();

            // Create a new stage for the Create Course window
            Stage stage = new Stage();
            stage.setTitle("Create Course/Meeting");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteStudent() {
        String selectedStudent = studentsListView.getSelectionModel().getSelectedItem();
        String selectedCourseCode = coursesListView.getSelectionModel().getSelectedItem();
        if (selectedStudent != null) {
            // Call the delete method and update the UI
            deleteStudentFromCourse(selectedCourseCode, selectedStudent);
        }
    }

    @FXML
    private void handleAddStudent() {
        try {
            // Get the selected course from the list
            String selectedCourse = coursesListView.getSelectionModel().getSelectedItem();

            // Ensure a course is selected
            if (selectedCourse == null) {
                showAlert(Alert.AlertType.WARNING, "No Course Selected", "Please select a course before adding students.");
                return;
            }

            // Load the StudentListUI.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("StudentListUI.fxml"));
            Parent root = loader.load();


            // Retrieve the controller for the student list view
            StudentListController controller = loader.getController();
            controller.disableDoneButton(); //Disable Done Button in Student Addition Part
            MainViewController mainViewController = MainViewController.getInstance();
            controller.setMainViewController(mainViewController);

            // Fetch course capacity and current student count
            int courseCapacity = dbManager.getCourseCapacity(selectedCourse);
            int currentStudentCount = dbManager.getCourseStudentCount(selectedCourse);

            // Calculate the remaining capacity
            int remainingCapacity = courseCapacity - currentStudentCount;

            // Debugging output to test values
            System.out.println("Course: " + selectedCourse);
            System.out.println("Capacity: " + courseCapacity);
            System.out.println("Current Student Count: " + currentStudentCount);
            System.out.println("Remaining Capacity: " + remainingCapacity);

            // If no remaining capacity, show warning and exit
            if (remainingCapacity <= 0) {
                showAlert(Alert.AlertType.WARNING, "Course Full", "The selected course is already full.");
                return;
            }

            // Pass the selected course and remaining capacity to the StudentListController
            controller.listMissingStudents(selectedCourse, remainingCapacity);

            // Additional setup in the controller (like button enabling)
            controller.btnAddSetAvailable();

            // Display the StudentListUI in a new window
            studentListAddstage.setTitle("Student List");
            studentListAddstage.setScene(new Scene(root));
            if (studentListAddstage.isShowing()){
                studentListAddstage.toFront();
            }
            if (studentListAddstage.isIconified()) {
                studentListAddstage.setIconified(false);
            }
            else {
                studentListAddstage.show();
            }


        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while opening the student list.");
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

    public void addStudentToCourse(String courseCode, String studentName) {
        try {
            // Confirm addition
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Addition");
            confirmationAlert.setHeaderText("Are you sure you want to add the student?");
            confirmationAlert.setContentText("Student: " + studentName);

            // Handle user response
            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Add the student to the course in the database
                boolean success = dbManager.addStudentToCourse(courseCode, studentName);
                if (success) {
                    // Update the ListView
                    studentsListView.getItems().add(studentName);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Student added to the course successfully.");

                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to add student to the course.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while adding the student.");
        }
    }

    @FXML
    private void handleHelp() {
        try {
            // Load the HelpManual UI from the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClassAssignmentSystem/HelpManual.fxml"));
            Parent root = loader.load();

            // Create a new stage for the Help Manual
            Stage stage = new Stage();
            stage.setTitle("Help Manual");
            stage.setScene(new Scene(root, 800, 600)); // You can adjust the size as needed
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open Help Manual.");
        }
    }

    public ListView<String> getCourseListView() {
        return coursesListView;
    }

    public DatabaseManager getDbManager() {
        return dbManager;
    }

    public ListView<String> getStudentListView() {
        return studentsListView;
    }

}
