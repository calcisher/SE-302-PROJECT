package ClassAssignmentSystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalTime;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CreateCourseController {

    @FXML
    private TextField txtCourseID;
    @FXML
    private TextField txtLecturer;
    @FXML
    private ChoiceBox<String> choiceDay;
    @FXML
    private ChoiceBox<String> choiceStartTime;
    @FXML
    private ChoiceBox<Integer> choiceDuration;

    private final List<Student> selectedStudents = new java.util.ArrayList<>();
    private Classroom assignedClassroom;

    @FXML
    private void initialize() {
        // Populate Day ChoiceBox
        choiceDay.setItems(FXCollections.observableArrayList(
                "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"
        ));

        // Add listener to Day ChoiceBox to update Start Time ChoiceBox
        choiceDay.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateStartTimeChoices(newValue);
        });

        // Add listener to Start Time ChoiceBox to update Duration ChoiceBox
        choiceStartTime.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateDurationChoices(newValue);
        });
    }

    @FXML
    private void handleAssignStudents() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("studentListUI.fxml"));
            Parent root = loader.load();

            StudentListController studentListController = loader.getController();
            studentListController.setSelectionMode(true); // Enable selection mode
            Stage stage = new Stage();
            stage.setTitle("Select Students");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            selectedStudents.clear();
            selectedStudents.addAll(studentListController.getSelectedStudents());
            System.out.println("Selected Students: " + selectedStudents.size());
             // After selecting students, refresh start time choices if day is already selected
            String selectedDay = choiceDay.getValue();
            if (selectedDay != null) {
                updateStartTimeChoices(selectedDay);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open student selection window.");
        }
    }
    private void updateStartTimeChoices(String day) {
        choiceStartTime.getItems().clear();
        choiceDuration.getItems().clear();

        if (day == null || selectedStudents.isEmpty()) {
            return;
        }

        try {
            // Fetch common free times for selected students on the selected day
            ObservableList<String> freeTimes = DatabaseManager.getCommonFreeTimes(selectedStudents, day);
            choiceStartTime.setItems(freeTimes);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to retrieve free times.");
        }
    }

    private void updateDurationChoices(String startTimeStr) {
        choiceDuration.getItems().clear();

        if (startTimeStr == null || selectedStudents.isEmpty()) {
            return;
        }

        try {
            // Parse the selected start time
            LocalTime startTime = LocalTime.parse(startTimeStr, java.time.format.DateTimeFormatter.ofPattern("H:mm"));

            // Determine the maximum possible duration based on continuous free slots
            int maxDuration = DatabaseManager.getMaxContinuousFreeSlots(selectedStudents, choiceDay.getValue(), startTime);

            // Populate Duration ChoiceBox with values from 1 to maxDuration
            if (maxDuration > 0) {
                ObservableList<Integer> durations = FXCollections.observableArrayList();
                for (int i = 1; i <= maxDuration; i++) {
                    durations.add(i);
                }
                choiceDuration.setItems(durations);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to determine duration options.");
        }
    }


    @FXML
    private void handleAssignClass() {
        try {
            String day = choiceDay.getValue();
            String startTime = choiceStartTime.getValue();
            Integer duration = choiceDuration.getValue();

            if (day == null || startTime == null || duration == null || selectedStudents.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please select day, start time, duration, and assign students.");
                return;
            }

            DatabaseManager dbManager = new DatabaseManager("university.db");
            assignedClassroom = dbManager.findAvailableClass(day, startTime, duration);

            if (assignedClassroom != null) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Classroom assigned: " + assignedClassroom.getName());
            } else {
                showAlert(Alert.AlertType.WARNING, "No Available Classrooms", "No classrooms are available for the specified time.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to assign a classroom.");
        }

    }

    @FXML
    private void handleDone() {
        try {
            String courseID = txtCourseID.getText();
            String lecturer = txtLecturer.getText();
            String day = choiceDay.getValue();
            String startTime = choiceStartTime.getValue();
            Integer duration = choiceDuration.getValue();

            if (courseID.isEmpty() || lecturer.isEmpty() || day == null || startTime == null || duration == null || assignedClassroom == null) {
                showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please fill all fields and assign a classroom.");
                return;
            }

            DatabaseManager dbManager = new DatabaseManager("university.db");
            dbManager.insertNewCourse(courseID, day + " " + startTime, duration, lecturer, selectedStudents, assignedClassroom);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Course created successfully!");
            ((Stage) txtCourseID.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create the course.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

