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
import java.util.List;

import static ClassAssignmentSystem.CSVImporter.showAlert;

public class StudentListController {

    ListView<String> studentListView;
    ListView<String> courseListView;
    DatabaseManager dbManager;
    @FXML
    private TableView<Student> studentsTable;

    @FXML
    private TableColumn<Student, String> studentNameColumn;

    @FXML
    private TableColumn<Student, CheckBox> selectColumn;

    @FXML
    private Button findAvailableTimeSlotsButton;

    @FXML
    private Button btnAdd;

    private final ObservableList<Student> studentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        selectColumn.setCellValueFactory(new PropertyValueFactory<>("selectBox"));

        // Disable the "Find Available Time Slots" button initially
        findAvailableTimeSlotsButton.setDisable(true);
        btnAdd.setDisable(true);

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

    public void btnAddSetAvailable() {
        btnAdd.setDisable(false);
    }
    public void listAllStudentsFromDatabase() {
        // Load student data from the database
        loadStudentsFromDatabase();
        // Set the data to the table
        studentsTable.setItems(studentList);
    }

    public void listMissingStudents(String courseName, int remainingCapacity) {
        // Load missing student data from the database
        loadMissingStudentsFromDatabase(courseName);

        // Limit selection to remaining capacity
        studentsTable.setItems(studentList);
        studentsTable.setRowFactory(tv -> {
            TableRow<Student> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Student selectedStudent = row.getItem();
                    if (studentsTable.getSelectionModel().getSelectedItems().size() > remainingCapacity) {
                        showAlert(Alert.AlertType.WARNING, "Selection Limit Reached", "You can only select up to " + remainingCapacity + " students.");
                    }
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

    private void loadMissingStudentsFromDatabase(String courseName) {
        ObservableList<String> studentNames = DatabaseManager.getStudentsNotInCourse(courseName);
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
        openFreeTimeSlots(studentList);
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
    public void getListViewsAndDBManager(ListView<String> courseListView, ListView<String> studentListView, DatabaseManager dbManager) {
        this.courseListView = courseListView;
        this.studentListView = studentListView;
        this.dbManager = dbManager;
    }
    @FXML
    private void handleAddStudents() {
        try {
            // Get the selected course
            String selectedCourse = courseListView.getSelectionModel().getSelectedItem();

            if (selectedCourse == null) {
                showAlert(Alert.AlertType.WARNING, "No Course Selected", "Please select a course before adding students.");
                return;
            }

            ObservableList<Student> selectedStudents = studentsTable.getSelectionModel().getSelectedItems();

            if (selectedStudents.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "No Students Selected", "Please select at least one student to add.");
                return;
            }

            for (Student student : selectedStudents) {
                boolean success = dbManager.addStudentToCourse(selectedCourse, student.getName());
                if (success) {
                    studentListView.getItems().add(student.getName());
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to add student: " + student.getName());
                }
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Selected students added to the course successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while adding students.");
        }
    }


}
