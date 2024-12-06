package ClassAssignmentSystem;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class StudentScheduleController {

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

        ObservableList<ScheduleEntry> scheduleData = FXCollections.observableArrayList(
                new ScheduleEntry("08:00 - 09:00", "SE115\nC201", "", "MATH240\nC301", "", "FENG101\nM206"),
                new ScheduleEntry("09:00 - 10:00", "", "SE302\nC202", "", "CE215\nM101", ""),
                new ScheduleEntry("10:00 - 11:00", "PHYS100\nML103", "", "CHEM100\nML104", "SE307\nC203", ""),
                new ScheduleEntry("11:00 - 12:00", "CE221\nM102", "MATH153\nML105", "", "", "SE350\nC204"),
                new ScheduleEntry("12:00 - 13:00", "", "", "", "", ""),
                new ScheduleEntry("13:00 - 14:00", "SE360\nC205", "", "", "SE380\nC206", ""),
                new ScheduleEntry("14:00 - 15:00", "", "CE323\nM201", "MATH250\nM01", "", "SE431\nC208"),
                new ScheduleEntry("15:00 - 16:00", "SE420\nC207", "", "", "CE342\nM202", ""),
                new ScheduleEntry("16:00 - 17:00", "", "FENG345\nML102", "", "CE345\nM203", "")
        );

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
