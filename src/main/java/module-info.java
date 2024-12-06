module com.example.se302project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.opencsv;


    opens ClassAssignmentSystem to javafx.fxml;
    exports ClassAssignmentSystem;
}