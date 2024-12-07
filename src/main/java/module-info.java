module com.example.se302project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.opencsv;
    requires org.slf4j;


    opens ClassAssignmentSystem to javafx.fxml;
    exports ClassAssignmentSystem;
}