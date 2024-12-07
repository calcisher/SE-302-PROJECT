package ClassAssignmentSystem;

import javafx.application.Application;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

public class CSVImporter extends Application {

    private File classroomFile;
    private File courseFile;
    private final DatabaseManager databaseManager = new DatabaseManager("university.db");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("CSV Importer");

        Button selectClassroomButton = new Button("Select Classroom CSV");
        Button selectCourseButton = new Button("Select Course CSV");
        Button importButton = new Button("Import Data");
        importButton.setDisable(true);

        Label classroomLabel = new Label("No classroom file selected.");
        Label courseLabel = new Label("No course file selected.");

        selectClassroomButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                classroomFile = selectedFile;
                classroomLabel.setText("Classroom File: " + selectedFile.getName());
            }
            toggleImportButton(importButton);
        });

        selectCourseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                courseFile = selectedFile;
                courseLabel.setText("Course File: " + selectedFile.getName());
            }
            toggleImportButton(importButton);
        });

        importButton.setOnAction(e -> {
            try {
                importClassroomData(classroomFile,databaseManager);
                importCourseData(courseFile,databaseManager);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Data imported successfully!");

            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while importing data.");
            }
        });

        VBox layout = new VBox(10, selectClassroomButton, classroomLabel, selectCourseButton, courseLabel, importButton);
        layout.setStyle("-fx-padding: 20;");

        primaryStage.setScene(new Scene(layout, 400, 300));
        primaryStage.show();

        String s = (String) DatabaseManager.selectInit("Courses","Course","Course = 'SE115'");
        System.out.println(s);
    }

    private void toggleImportButton(Button importButton) {
        importButton.setDisable(classroomFile == null || courseFile == null);
    }

    private void importClassroomData(File classroomFile, DatabaseManager db) {
        try {
            if (classroomFile != null) {
                List<String[]> classroomData = readCSV(classroomFile);
                String[] classroomColumns = classroomData.getFirst(); // First row is headers
                db.createTable("Classrooms", classroomColumns);
                db.insertClassroomData("Classrooms",classroomColumns, classroomData.subList(1, classroomData.size()));
            }
        }catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while importing data.");
        }
    }

    private void importCourseData(File courseFile, DatabaseManager db) {
        try {
            if (courseFile != null) {
                List<String[]> courseData = readCSV(courseFile);
                String[] courseColumns = courseData.getFirst(); // First row is headers
                db.createTable("Courses", courseColumns);
                db.insertCourseData("Courses", courseColumns, courseData.subList(1, courseData.size()));
            }
        }catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while importing data.");
        }

    }

    public static List<String[]> readCSV(File file) throws Exception {
        List<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                data.add(line.split(";"));  // Note: We're using ';' as delimiter
            }
        }
        return data;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
