package ClassAssignmentSystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class MainViewController {
    @FXML
    private void onManageCourse(ActionEvent event) {
        showAlert("Manage Course Clicked!");
    }

    @FXML
    private void onCreateMeeting(ActionEvent event) {
        showAlert("Create Meeting Clicked!");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Action");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
