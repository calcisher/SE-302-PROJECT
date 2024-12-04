module com.example.se302project {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.se302project to javafx.fxml;
    exports com.example.se302project;
}