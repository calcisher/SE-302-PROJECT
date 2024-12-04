module com.example.se302project {
    requires javafx.controls;
    requires javafx.fxml;


    opens ClassAssignmentSystem to javafx.fxml;
    exports ClassAssignmentSystem;
}