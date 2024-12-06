package ClassAssignmentSystem;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MainViewController {

    @FXML
    private Button showCourseListButton;

    @FXML
    private Button showClassListButton;

    @FXML
    public void initialize() {
        // Ders Listesi Butonu İçin Aksiyon
        showCourseListButton.setOnAction(event -> showCourseList());

        // Sınıf Listesi Butonu İçin Aksiyon
        showClassListButton.setOnAction(event -> showClassList());
    }

    private void showCourseList() {
        Stage courseListStage = new Stage();
        courseListStage.setTitle("Course List");

        ObservableList<String> courses = FXCollections.observableArrayList(
                "SE115",
                "SE302",
                "SE307",
                "SE350",
                "SE360"
        );

        ListView<String> courseListView = new ListView<>(courses);
        ScrollPane scrollPane = new ScrollPane(courseListView);

        VBox layout = new VBox(new Label("Courses"), scrollPane);
        Scene scene = new Scene(layout, 300, 400);

        courseListStage.setScene(scene);
        courseListStage.show();
    }

    private void showClassList() {
        Stage classListStage = new Stage();
        classListStage.setTitle("Class List");

        ObservableList<String> classes = FXCollections.observableArrayList(
                "C201",
                "C202",
                "C203",
                "C204",
                "C205"
        );

        ListView<String> classListView = new ListView<>(classes);
        ScrollPane scrollPane = new ScrollPane(classListView);

        VBox layout = new VBox(new Label("Classes"), scrollPane);
        Scene scene = new Scene(layout, 300, 400);

        classListStage.setScene(scene);
        classListStage.show();
    }
}
