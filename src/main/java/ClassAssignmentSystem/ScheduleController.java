package ClassAssignmentSystem;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.ObservableList;

import java.util.List;

public class ScheduleController {
    @FXML
    private TableView<ScheduleEntry> scheduleTable;
    @FXML
    private TableColumn<ScheduleEntry, String> timeColumn;
    @FXML
    private TableColumn<ScheduleEntry, String> mondayColumn;
    @FXML
    private TableColumn<ScheduleEntry, String> tuesdayColumn;
    @FXML
    private TableColumn<ScheduleEntry, String> wednesdayColumn;
    @FXML
    private TableColumn<ScheduleEntry, String> thursdayColumn;
    @FXML
    private TableColumn<ScheduleEntry, String> fridayColumn;

    @FXML
    public void initialize() {
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        mondayColumn.setCellValueFactory(new PropertyValueFactory<>("monday"));
        tuesdayColumn.setCellValueFactory(new PropertyValueFactory<>("tuesday"));
        wednesdayColumn.setCellValueFactory(new PropertyValueFactory<>("wednesday"));
        thursdayColumn.setCellValueFactory(new PropertyValueFactory<>("thursday"));
        fridayColumn.setCellValueFactory(new PropertyValueFactory<>("friday"));
    }

    public void loadStudentSchedule(String studentName) {
        ObservableList<ScheduleEntry> scheduleData = DatabaseManager.getStudentSchedule(studentName);
        scheduleTable.setItems(scheduleData);
    }
    public void loadCourseSchedule(String className) {
        ObservableList<ScheduleEntry> scheduleData = DatabaseManager.getClassSchedule(className);
        scheduleTable.setItems(scheduleData);
    }
    public void loadFreeTimeSchedule(List<Student> students) {
        ObservableList<ScheduleEntry> scheduleData = DatabaseManager.getFreeTimeSchedule(students);
        scheduleTable.setItems(scheduleData);
    }


    public static class ScheduleEntry {
        private String time;
        private String monday;
        private String tuesday;
        private String wednesday;
        private String thursday;
        private String friday;

        public ScheduleEntry(String time, String monday, String tuesday, String wednesday, String thursday, String friday) {
            this.time = time;
            this.monday = monday;
            this.tuesday = tuesday;
            this.wednesday = wednesday;
            this.thursday = thursday;
            this.friday = friday;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getMonday() {
            return monday;
        }

        public void setMonday(String monday) {
            this.monday = monday;
        }

        public String getTuesday() {
            return tuesday;
        }

        public void setTuesday(String tuesday) {
            this.tuesday = tuesday;
        }

        public String getWednesday() {
            return wednesday;
        }

        public void setWednesday(String wednesday) {
            this.wednesday = wednesday;
        }

        public String getThursday() {
            return thursday;
        }

        public void setThursday(String thursday) {
            this.thursday = thursday;
        }

        public String getFriday() {
            return friday;
        }

        public void setFriday(String friday) {
            this.friday = friday;
        }
    }
}