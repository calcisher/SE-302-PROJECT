package ClassAssignmentSystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.ArrayList;

import java.util.List;

public class StudentListController {

    @FXML
    private TableView<Student> studentsTable;

    @FXML
    private TableColumn<Student, String> studentNameColumn;

    @FXML
    private TableColumn<Student, CheckBox> selectColumn;

    @FXML
    private Button findAvailableTimeSlotsButton;

    @FXML
    private Button btnDone;

    private final ObservableList<Student> studentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        selectColumn.setCellValueFactory(new PropertyValueFactory<>("selectBox"));

        // Load student data from the database
        loadStudentsFromDatabase();

        studentsTable.setItems(studentList);

        // Disable the "Find Available Time Slots" button initially
        findAvailableTimeSlotsButton.setDisable(true);

        // Add a listener to enable the button when any checkbox is selected
        studentList.forEach(student -> student.getSelectBox().selectedProperty().addListener((obs, oldVal, newVal) -> {
            boolean anySelected = studentList.stream().anyMatch(s -> s.getSelectBox().isSelected());
            findAvailableTimeSlotsButton.setDisable(!anySelected);
        }));

        // Add double-click listener for the table rows
        studentsTable.setRowFactory(tv -> {
            TableRow<Student> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Student selectedStudent = row.getItem();
                    openStudentSchedule(selectedStudent.getName());
                }
            });
            return row;
        });
    }

    private void loadStudentsFromDatabase() {
        ObservableList<String> studentNames = DatabaseManager.getDistinctStudentNames();
        for (String name : studentNames) {
            studentList.add(new Student(name, new CheckBox()));
        }
    }

    @FXML
    private void handleFindAvailableTimeSlots(ActionEvent event) {
        List<Student> selectedStudents = new java.util.ArrayList<>(List.of());
        for (Student student : studentList) {
            if (student.getSelectBox().isSelected()) {
                selectedStudents.add(student);
            }
        }
        openFreeTimeSlots(selectedStudents); // previously "studentList", program selected all students list not only selected ones
    }

    public static void openStudentSchedule(String studentName) {
        try {
            FXMLLoader loader = new FXMLLoader(StudentListController.class.getResource("scheduleUI.fxml"));
            Parent root = loader.load();

            ScheduleController controller = loader.getController();
            controller.loadStudentSchedule(studentName);

            Stage stage = new Stage();
            stage.setTitle("Schedule for " + studentName);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openFreeTimeSlots(List<Student> students) {
        try {
            FXMLLoader loader = new FXMLLoader(StudentListController.class.getResource("scheduleUI.fxml"));
            Parent root = loader.load();

            ScheduleController controller = loader.getController();
            controller.loadFreeTimeSchedule(students);

            Stage stage = new Stage();
            stage.setTitle("Free Times For Students");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleClearChoices() {
        for (Student student : studentList) {
            student.getSelectBox().setSelected(false);
        }
    }

    @FXML
    private void handleDone() {
        // Close the student selection window
        Stage stage = (Stage) btnDone.getScene().getWindow();
        stage.close();
    }

    private boolean selectionMode = false;

    public void setSelectionMode(boolean enable) {
        selectionMode = enable;
        selectColumn.setVisible(enable);
    }

    public List<Student> getSelectedStudents() {
        List<Student> selected = new ArrayList<>();
        for (Student student : studentList) {
            if (student.getSelectBox().isSelected()) {
                selected.add(student);
            }
        }
        return selected;
    }



}
