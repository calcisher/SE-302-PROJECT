package ClassAssignmentSystem;

import java.sql.*;
import java.util.List;

public class DatabaseManager {
    private String databasePath;

    public DatabaseManager(String databasePath) {
        this.databasePath = databasePath;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + databasePath);
    }

    /**
     * Creates the normalized tables: Courses, Students, and Course_Students.
     */
    public void createNormalizedTables() {
        String createCoursesTable = "CREATE TABLE IF NOT EXISTS Courses ("
                + "CourseID TEXT PRIMARY KEY,"
                + "TimeToStart TEXT,"
                + "DurationInLectureHours INTEGER,"
                + "Lecturer TEXT"
                + ");";

        String createStudentsTable = "CREATE TABLE IF NOT EXISTS Students ("
                + "StudentID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "StudentName TEXT UNIQUE"
                + ");";

        String createCourseStudentsTable = "CREATE TABLE IF NOT EXISTS Course_Students ("
                + "CourseID TEXT,"
                + "StudentID INTEGER,"
                + "FOREIGN KEY (CourseID) REFERENCES Courses(CourseID),"
                + "FOREIGN KEY (StudentID) REFERENCES Students(StudentID),"
                + "PRIMARY KEY (CourseID, StudentID)"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createCoursesTable);
            stmt.execute(createStudentsTable);
            stmt.execute(createCourseStudentsTable);
            System.out.println("Normalized tables created or already exist.");
        } catch (SQLException e) {
            System.err.println("Error creating normalized tables.");
            e.printStackTrace();
        }
    }

    /**
     * Inserts a course into the Courses table.
     *
     * @param courseID               The unique identifier for the course.
     * @param timeToStart            The start time of the course.
     * @param durationInLectureHours The duration of the course in hours.
     * @param lecturer               The lecturer of the course.
     */
    public void insertCourse(String courseID, String timeToStart, int durationInLectureHours, String lecturer) {
        String sql = "INSERT OR IGNORE INTO Courses (CourseID, TimeToStart, DurationInLectureHours, Lecturer) VALUES (?, ?, ?, ?);";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courseID);
            pstmt.setString(2, timeToStart);
            pstmt.setInt(3, durationInLectureHours);
            pstmt.setString(4, lecturer);
            pstmt.executeUpdate();
            System.out.println("Inserted/Existing Course: " + courseID);
        } catch (SQLException e) {
            System.err.println("Error inserting course: " + courseID);
            e.printStackTrace();
        }
    }

    /**
     * Inserts a student into the Students table and returns the StudentID.
     *
     * @param studentName The name of the student.
     * @return The StudentID of the inserted or existing student.
     */
    public int insertStudent(String studentName) {
        String insertSql = "INSERT OR IGNORE INTO Students (StudentName) VALUES (?);";
        String selectSql = "SELECT StudentID FROM Students WHERE StudentName = ?;";

        try (Connection conn = connect();
             PreparedStatement insertPstmt = conn.prepareStatement(insertSql);
             PreparedStatement selectPstmt = conn.prepareStatement(selectSql)) {

            // Insert the student if not exists
            insertPstmt.setString(1, studentName);
            insertPstmt.executeUpdate();

            // Retrieve the StudentID
            selectPstmt.setString(1, studentName);
            ResultSet rs = selectPstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("StudentID");
            } else {
                throw new SQLException("Failed to retrieve StudentID for: " + studentName);
            }
        } catch (SQLException e) {
            System.err.println("Error inserting/selecting student: " + studentName);
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Inserts a relationship between a course and a student into the Course_Students table.
     *
     * @param courseID  The CourseID.
     * @param studentID The StudentID.
     */
    public void insertCourseStudent(String courseID, int studentID) {
        String sql = "INSERT OR IGNORE INTO Course_Students (CourseID, StudentID) VALUES (?, ?);";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courseID);
            pstmt.setInt(2, studentID);
            pstmt.executeUpdate();
            System.out.println("Linked Course: " + courseID + " with StudentID: " + studentID);
        } catch (SQLException e) {
            System.err.println("Error linking CourseID: " + courseID + " with StudentID: " + studentID);
            e.printStackTrace();
        }
    }

    /**
     * Sanitizes table and column names by replacing non-alphanumeric characters with underscores.
     *
     * @param name The original name.
     * @return The sanitized name.
     */
    private String sanitizeName(String name) {
        return name.trim().replaceAll("[^a-zA-Z0-9_]", "_");
    }
}
