package ClassAssignmentSystem;
import javafx.scene.control.*;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

public class CSVImporter {

    /*private File classroomFile;
    private File courseFile;
    private final DatabaseManager databaseManager = new DatabaseManager("university.db");
     */

    public static void importClassroomData(File classroomFile, DatabaseManager db) {
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

    public static void importCourseData(File courseFile, DatabaseManager db) {
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

    public static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
