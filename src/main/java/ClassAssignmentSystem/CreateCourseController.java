package ClassAssignmentSystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CreateCourseController {

    @FXML
    private TextField txtCourseID;
    @FXML
    private TextField txtLecturer;
    @FXML
    private TextField txtDay;
    @FXML
    private TextField txtStartHour;
    @FXML
    private TextField txtDuration;

    private final List<Student> selectedStudents = new ArrayList<>();
    private Classroom assignedClassroom;

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
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open student selection window.");
        }
    }

    @FXML
    private void handleAssignClass() {
        try {
            DatabaseManager dbManager = new DatabaseManager("university.db");
            String day = txtDay.getText();
            String startHour = txtStartHour.getText();
            int duration = Integer.parseInt(txtDuration.getText());
            assignedClassroom = dbManager.findAvailableClass(day, startHour, duration);

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
            String day = txtDay.getText();
            String startHour = txtStartHour.getText();
            int duration = Integer.parseInt(txtDuration.getText());

            if (courseID.isEmpty() || lecturer.isEmpty() || day.isEmpty() || startHour.isEmpty() || duration <= 0 || assignedClassroom == null) {
                showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please fill all fields and assign a classroom.");
                return;
            }

            DatabaseManager dbManager = new DatabaseManager("university.db");
            dbManager.insertNewCourse(courseID, day + " " + startHour, duration, lecturer, selectedStudents, assignedClassroom);

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

