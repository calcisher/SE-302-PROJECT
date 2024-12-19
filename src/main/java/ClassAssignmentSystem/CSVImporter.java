package ClassAssignmentSystem;
import javafx.scene.control.*;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CSVImporter {

    public static void importClassroomData(File classroomFile, DatabaseManager db) {
        try {
            if (classroomFile != null) {
                List<String[]> classroomData = readCSV(classroomFile);
                String[] classroomColumns = classroomData.getFirst(); // First row is headers

                // Define column data types
                Map<String, String> columnsWithTypes = new LinkedHashMap<>();
                for (String column : classroomColumns) {
                    if (column.equalsIgnoreCase("Capacity")) {
                        columnsWithTypes.put(column, "INTEGER");
                    } else {
                        columnsWithTypes.put(column, "TEXT");
                    }
                }

                db.createTable("Classrooms", columnsWithTypes);
                db.insertClassroomData("Classrooms", classroomColumns, classroomData.subList(1, classroomData.size()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while importing data.");
        }
    }


    public static void importCourseData(File courseFile, DatabaseManager db) {
        try {
            if (courseFile != null) {
                List<String[]> courseData = readCSV(courseFile);
                String[] courseColumns = courseData.getFirst(); // First row is headers

                //Define column data types
                Map<String, String> columnsWithTypes = new LinkedHashMap<>();
                for (String column : courseColumns) {
                    switch (column.toLowerCase()) {
                        case "durationinlecturehours":
                            columnsWithTypes.put(column, "INTEGER");
                            break;
                        case "classroom":
                            columnsWithTypes.put(column, "TEXT");
                            break;
                        default:
                            columnsWithTypes.put(column, "TEXT");
                            break;
                    }
                }

                db.createTable("Courses", columnsWithTypes);
                db.addClassroomColumnIfMissing();
                db.insertCourseData("Courses", courseColumns, courseData.subList(1, courseData.size()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while importing data.");
        }
    }


    public static List<String[]> readCSV(File file) throws Exception {
        List<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                data.add(line.split(";"));
            }
        }
        return data;
    }

    public static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
