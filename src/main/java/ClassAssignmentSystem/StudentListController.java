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

import static ClassAssignmentSystem.CSVImporter.showAlert;

public class StudentListController {


    private MainViewController mainViewController;
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
    private Button btnAdd;

    public void initialize() {
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        selectColumn.setCellValueFactory(new PropertyValueFactory<>("selectBox"));

        // Load student data from the database
        loadStudentsFromDatabase();

        studentsTable.setItems(studentList);

        // Disable the "Find Available Time Slots" button initially
        findAvailableTimeSlotsButton.setDisable(true);
        btnAdd.setDisable(true);

        int remainingCapacity = mainViewController != null ? mainViewController.getDbManager().getRemainingCapacity(null): Integer.MAX_VALUE;
        enforceCapacityLimit(remainingCapacity);

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

    /*private ObservableList<Student> getSelectedStudents() {
        return studentList.filtered(Student::isSelected);
    }*/


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

    // Set method for MainViewController
    public void setMainViewController(MainViewController mainViewController) {
        this.mainViewController = mainViewController;
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


    @FXML
    private void handleAddStudents() {
        try {
            if (mainViewController == null) {
                throw new IllegalStateException("MainViewController is not set.");
            }

            // Get the selected course
            String selectedCourse = mainViewController.getCourseListView().getSelectionModel().getSelectedItem();

            if (selectedCourse == null) {
                mainViewController.showAlert(Alert.AlertType.WARNING, "No Course Selected", "Please select a course before adding students.");
                return;
            }

            // Calculate remaining capacity
            int remainingCapacity = mainViewController.getDbManager().getRemainingCapacity(selectedCourse);

            if (remainingCapacity <= 0) {
                mainViewController.showAlert(Alert.AlertType.WARNING, "Capacity Full", "No remaining capacity for this course.");
                return;
            }

            // Get selected students
            ObservableList<Student> selectedStudents = FXCollections.observableArrayList();
            for (Student student : studentsTable.getItems()) {
                if (student.isSelected()) { // Check if the student's checkbox is selected
                    selectedStudents.add(student);
                }
            }

            if (selectedStudents.isEmpty()) {
                mainViewController.showAlert(Alert.AlertType.WARNING, "No Students Selected", "Please select at least one student to add.");
                return;
            }

            // Ensure selections do not exceed remaining capacity
            if (selectedStudents.size() > remainingCapacity) {
                mainViewController.showAlert(Alert.AlertType.WARNING, "Capacity Exceeded", "You cannot select more than the remaining capacity (" + remainingCapacity + ").");
                return;
            }

            // Add selected students to the course
            for (Student student : selectedStudents) {
                mainViewController.addStudentToCourse(selectedCourse, student.getName());
            }

            mainViewController.showAlert(Alert.AlertType.INFORMATION, "Success", "Selected students added to the course successfully.");
            Stage stage = (Stage) studentsTable.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            if (mainViewController != null) {
                mainViewController.showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while adding students.");
            }
        }
    }

    private void enforceCapacityLimit(int remainingCapacity) {
        studentList.forEach(student -> {
            student.getSelectBox().selectedProperty().addListener((observable, oldValue, newValue) -> {
                long selectedCount = studentList.stream().filter(s -> s.getSelectBox().isSelected()).count();

                if (selectedCount > remainingCapacity) {
                    student.getSelectBox().setSelected(false); // Seçimi geri al
                    showAlert(Alert.AlertType.WARNING, "Capacity Exceeded", "You cannot select more than the remaining capacity (" + remainingCapacity + ").");
                }

                // Butonları dinamik olarak aktif/pasif yap
                boolean anySelected = studentList.stream().anyMatch(s -> s.getSelectBox().isSelected());
                findAvailableTimeSlotsButton.setDisable(!anySelected);
                btnAdd.setDisable(!anySelected);
            });
        });
    }






}
