package ClassAssignmentSystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static String databasePath;

    public DatabaseManager(String databasePath) {
        this.databasePath = databasePath;
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("Connected to the database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + databasePath);
    }

    public void createTable(String tableName, String[] columns) throws SQLException {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
        for (String column : columns) {
            sql.append("\"").append(column).append("\" TEXT,");
        }
        sql.deleteCharAt(sql.length() - 1).append(")");
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql.toString());
        }
    }

    //For adding Course CSV data to Courses Table.
    public void insertCourseData(String coursesTableName, String[] columnNames, List<String[]> data) throws SQLException {
        if (data.isEmpty()) return;

        StringBuilder coursesSql = new StringBuilder("INSERT INTO ").append(coursesTableName).append(" (");
        for (String column : columnNames) {
            coursesSql.append("\"").append(column).append("\",");
        }
        coursesSql.deleteCharAt(coursesSql.length() - 1).append(") VALUES (");
        coursesSql.append("?,".repeat(columnNames.length));
        coursesSql.deleteCharAt(coursesSql.length() - 1).append(")");

        //For test and debugging purposes
        System.out.println("Generated SQL: " + coursesSql);

        try (Connection conn = getConnection();
             PreparedStatement coursesStmt = conn.prepareStatement(coursesSql.toString());
             PreparedStatement checkExistStmt = conn.prepareStatement("SELECT COUNT(*) FROM " + coursesTableName + " WHERE Course = ? AND TimeToStart = ? AND Lecturer = ? AND Students = ?")) {

            for (String[] row : data) {
                // Ensure there are at least 4 columns ("Course","TimeToStart","DurationInLectureHours","Lecturer","Students") for Course data CSV
                if (row.length < 4) {
                    System.err.println("Warning: Skipping row with insufficient data.");
                    continue;
                }

                // Insert data into Courses table for each student explicitly every course
                for (int colIndex = 4; colIndex < row.length; colIndex++) {
                    String student = row[colIndex].trim();

                    if (!student.isEmpty()) {
                        // Check if this data already exists in the database
                        checkExistStmt.setString(1, row[0]); // Course
                        checkExistStmt.setString(2, row[1]); // TimeToStart
                        checkExistStmt.setString(3, row[3]); // Lecturer
                        checkExistStmt.setString(4, student); // Student

                        ResultSet rs = checkExistStmt.executeQuery();
                        rs.next();
                        int count = rs.getInt(1);

                        if (count == 0) {
                            // If the entry doesn't exist, insert the new data
                            coursesStmt.setString(1, row[0]); // Course
                            coursesStmt.setString(2, row[1]); // TimeToStart
                            coursesStmt.setString(3, row[2]); // DurationInLectureHours
                            coursesStmt.setString(4, row[3]); // Lecturer
                            coursesStmt.setString(5, student); // Student

                            try {
                                coursesStmt.executeUpdate();
                            } catch (SQLException e) {
                                System.err.println("Error inserting into Courses table: " + e.getMessage());
                            }
                        } else {
                            //For test
                            System.out.println("Skipping duplicate entry: " + student + " for course " + row[0]);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
            throw e;
        }
    }

    // For adding Classroom CSV data to Classrooms Table.
    public void insertClassroomData(String tableName, String[] columnNames, List<String[]> data) throws SQLException {
        if (data.isEmpty()) return;

        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        for (String column : columnNames) {
            sql.append("\"").append(column).append("\",");
        }
        sql.deleteCharAt(sql.length() - 1).append(") VALUES (");
        sql.append("?,".repeat(columnNames.length));
        sql.deleteCharAt(sql.length() - 1).append(")");

        // For test and debugging purposes
        System.out.println("Generated SQL: " + sql);

        try (Connection conn = getConnection();
             PreparedStatement classroomsStmt = conn.prepareStatement(sql.toString())) {

            for (String[] row : data) {  // Skip the header row
                if (row.length != columnNames.length) {
                    System.err.println("Warning: Skipping row with mismatched columns.");
                    continue;
                }

                for (int i = 0; i < columnNames.length; i++) {
                    classroomsStmt.setString(i + 1, row[i]);
                }

                try {
                    classroomsStmt.executeUpdate();
                } catch (SQLException e) {
                    System.err.println("Error inserting into Classrooms table: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
            throw e;
        }
    }

    // Read CSV file and return list of String arrays
    public List<String[]> readCSV(File file) throws Exception {
        List<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                data.add(line.split(";"));  // Note: Using ';' as delimiter
            }
        }
        return data;
    }

    // Retrieve all courses
    public List<String> getAllCourses() throws SQLException {
        List<String> courses = new ArrayList<>();
        String query = "SELECT Course FROM Courses";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                courses.add(rs.getString("Course"));
            }
        }
        return courses;
    }

    // Retrieve all classrooms
    public List<String> getAllClassrooms() throws SQLException {
        List<String> classrooms = new ArrayList<>();
        String query = "SELECT Classroom FROM Classrooms";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                classrooms.add(rs.getString("Classroom"));
            }
        }
        return classrooms;
    }

    // Assign courses to classrooms (simple assignment for demonstration)
    public boolean assignCoursesToClassrooms() throws SQLException {
        String selectCourses = "SELECT Course FROM Courses WHERE Course NOT IN (SELECT Course FROM Course)";
        String selectClassrooms = "SELECT Classroom, Capacity FROM Classrooms";
        String insertAssignment = "INSERT INTO Course (Course, Classroom) VALUES (?, ?)";

        try (Connection conn = getConnection();
             Statement courseStmt = conn.createStatement();
             ResultSet courseRs = courseStmt.executeQuery(selectCourses);
             Statement classStmt = conn.createStatement();
             ResultSet classRs = classStmt.executeQuery(selectClassrooms);
             PreparedStatement insertStmt = conn.prepareStatement(insertAssignment)) {

            List<String> availableClassrooms = new ArrayList<>();
            while (classRs.next()) {
                availableClassrooms.add(classRs.getString("Classroom"));
            }

            int classroomIndex = 0;
            while (courseRs.next()) {
                String courseId = courseRs.getString("Course");
                if (classroomIndex >= availableClassrooms.size()) {
                    // Not enough classrooms to assign
                    return false;
                }
                String classroomId = availableClassrooms.get(classroomIndex);
                insertStmt.setString(1, courseId);
                insertStmt.setString(2, classroomId);
                insertStmt.executeUpdate();
                classroomIndex++;
            }

            return true;
        } catch (SQLException e) {
            System.err.println("Error during course assignment: " + e.getMessage());
            return false;
        }
    }

    // Retrieve course details
    public Course getCourseDetails(String courseCode) throws SQLException {
        String query = "SELECT * FROM Courses WHERE Course = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, courseCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    //String courseCode = rs.getString("CourseCode");
                    String timeToStart = rs.getString("TimeToStart");
                    int duration = Integer.parseInt(rs.getString("Duration"));
                    String lecturer = rs.getString("Lecturer");
                    Classroom assignedClassroom = new Classroom(getAssignedClassroom(courseCode),getClassroomCapacity(courseCode));
                    return new Course(courseCode, timeToStart, duration, lecturer, assignedClassroom);
                }
            }
        }
        return null;
    }

    // Retrieve assigned classroom for a course
    private String getAssignedClassroom(String courseCode) throws SQLException {
        String query = "SELECT Classroom FROM Courses WHERE Course = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, courseCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("ClassroomName");
                }
            }
        }
        return null;
    }

    // Retrieve classroom details
    public Classroom getClassroomDetails(String classroomName) throws SQLException {
        String query = "SELECT * FROM Classrooms WHERE Classroom = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, classroomName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int capacity = Integer.parseInt(rs.getString("Capacity"));
                    return new Classroom(classroomName, capacity);
                }
            }
        }
        return null;
    }

    // Retrieve students for a course
    public List<String> getStudentsForCourse(String courseID) throws SQLException {
        List<String> students = new ArrayList<>();
        String query = "SELECT Students FROM Courses WHERE Course = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, courseID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(rs.getString("StudentName"));
                }
            }
        }
        return students;
    }

    // Retrieve classroom capacity
    public Integer getClassroomCapacity(String classroomName) throws SQLException {
        String query = "SELECT Capacity FROM Classrooms WHERE Classroom = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, classroomName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Integer.parseInt(rs.getString("Capacity"));
                }
            }
        }
        return 0;
    }

    // Example method for selecting data (already provided)
    public static Object selectInit(String entity, String attribute, String condition) {
        String selectQuery = "SELECT " + attribute + "\nFROM " + entity + "\nWHERE " + condition + ";";

        try (Connection conn = getConnection();
             PreparedStatement selectQueryStmt = conn.prepareStatement(selectQuery);
             ResultSet resultSet = selectQueryStmt.executeQuery()) {

            if (resultSet.next()) {
                Object result = resultSet.getObject(1);
                return result;
            }

        } catch (SQLException e) {
            System.out.println("Illegal argument.");
        }

        return null;
    }
}
