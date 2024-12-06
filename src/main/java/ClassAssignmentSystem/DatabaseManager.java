package ClassAssignmentSystem;

import java.sql.*;
import java.util.ArrayList;
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
     * Creates the normalized tables: Courses, Students, Course_Students, and Classrooms.
     */
    /**
     * Creates the normalized tables: Courses, Students, Course_Students, Classrooms, and Course_Classroom.
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

        String createClassroomsTable = "CREATE TABLE IF NOT EXISTS Classrooms ("
                + "ClassroomID TEXT PRIMARY KEY,"
                + "Capacity INTEGER"
                + ");";

        String createCourseClassroomTable = "CREATE TABLE IF NOT EXISTS Course_Classroom ("
                + "CourseID TEXT,"
                + "ClassroomID TEXT,"
                + "FOREIGN KEY (CourseID) REFERENCES Courses(CourseID),"
                + "FOREIGN KEY (ClassroomID) REFERENCES Classrooms(ClassroomID),"
                + "PRIMARY KEY (CourseID, ClassroomID)"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createCoursesTable);
            stmt.execute(createStudentsTable);
            stmt.execute(createCourseStudentsTable);
            stmt.execute(createClassroomsTable);
            stmt.execute(createCourseClassroomTable);
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
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Inserted Course: " + courseID);
            } else {
                System.out.println("Course already exists: " + courseID);
            }
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
        String normalizedStudentName = studentName.toUpperCase(); // Example normalization
        String insertSql = "INSERT OR IGNORE INTO Students (StudentName) VALUES (?);";
        String selectSql = "SELECT StudentID FROM Students WHERE StudentName = ?;";

        try (Connection conn = connect();
             PreparedStatement insertPstmt = conn.prepareStatement(insertSql);
             PreparedStatement selectPstmt = conn.prepareStatement(selectSql)) {

            // Insert the student if not exists
            insertPstmt.setString(1, normalizedStudentName);
            int affectedRows = insertPstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Inserted Student: " + normalizedStudentName);
            } else {
                System.out.println("Student already exists: " + normalizedStudentName);
            }

            // Retrieve the StudentID
            selectPstmt.setString(1, normalizedStudentName);
            ResultSet rs = selectPstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("StudentID");
            } else {
                throw new SQLException("Failed to retrieve StudentID for: " + normalizedStudentName);
            }
        } catch (SQLException e) {
            System.err.println("Error inserting/selecting student: " + normalizedStudentName);
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
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Linked Course: " + courseID + " with StudentID: " + studentID);
            } else {
                System.out.println("Link already exists for Course: " + courseID + " and StudentID: " + studentID);
            }
        } catch (SQLException e) {
            System.err.println("Error linking CourseID: " + courseID + " with StudentID: " + studentID);
            e.printStackTrace();
        }
    }

    /**
     * Inserts a classroom into the Classrooms table.
     *
     * @param classroomID The unique identifier for the classroom.
     * @param capacity    The capacity of the classroom.
     */
    public void insertClassroom(String classroomID, int capacity) {
        String sql = "INSERT OR IGNORE INTO Classrooms (ClassroomID, Capacity) VALUES (?, ?);";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, classroomID);
            pstmt.setInt(2, capacity);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Inserted Classroom: " + classroomID);
            } else {
                System.out.println("Classroom already exists: " + classroomID);
            }
        } catch (SQLException e) {
            System.err.println("Error inserting classroom: " + classroomID);
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all course IDs from the Courses table.
     *
     * @return A list of course IDs.
     */
    public List<String> getAllCourses() {
        List<String> courses = new ArrayList<>();
        String sql = "SELECT CourseID FROM Courses ORDER BY CourseID;";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                courses.add(rs.getString("CourseID"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving courses.");
            e.printStackTrace();
        }

        return courses;
    }

    /**
     * Retrieves all students enrolled in a specific course.
     *
     * @param courseID The CourseID.
     * @return A list of student names.
     */
    public List<String> getStudentsByCourse(String courseID) {
        List<String> students = new ArrayList<>();
        String sql = "SELECT s.StudentName FROM Students s "
                + "JOIN Course_Students cs ON s.StudentID = cs.StudentID "
                + "WHERE cs.CourseID = ? ORDER BY s.StudentName;";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courseID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                students.add(rs.getString("StudentName"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving students for course: " + courseID);
            e.printStackTrace();
        }

        return students;
    }

    /**
     * Retrieves comprehensive details of a specific course.
     *
     * @param courseID The CourseID.
     * @return A Course object containing all attributes, or null if not found.
     */
    public Course getCourseDetails(String courseID) {
        String sql = "SELECT CourseID, TimeToStart, DurationInLectureHours, Lecturer FROM Courses WHERE CourseID = ?;";
        Course course = null;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courseID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String id = rs.getString("CourseID");
                String timeToStart = rs.getString("TimeToStart");
                int duration = rs.getInt("DurationInLectureHours");
                String lecturer = rs.getString("Lecturer");
                course = new Course(id, timeToStart, duration, lecturer);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving details for course: " + courseID);
            e.printStackTrace();
        }

        return course;
    }

    /**
     * Retrieves all classroom IDs from the Classrooms table.
     *
     * @return A list of classroom IDs.
     */
    public List<String> getAllClassrooms() {
        List<String> classrooms = new ArrayList<>();
        String sql = "SELECT ClassroomID FROM Classrooms ORDER BY ClassroomID;";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                classrooms.add(rs.getString("ClassroomID"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving classrooms.");
            e.printStackTrace();
        }

        return classrooms;
    }

    /**
     * Retrieves the capacity of a specific classroom.
     *
     * @param classroomID The ClassroomID.
     * @return The capacity of the classroom, or -1 if not found.
     */
    public int getClassroomCapacity(String classroomID) {
        String sql = "SELECT Capacity FROM Classrooms WHERE ClassroomID = ?;";
        int capacity = -1;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, classroomID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                capacity = rs.getInt("Capacity");
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving capacity for classroom: " + classroomID);
            e.printStackTrace();
        }

        return capacity;
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

    /**
     * Assigns a classroom to a course.
     *
     * @param courseID    The CourseID.
     * @param classroomID The ClassroomID.
     * @return True if assignment is successful, false otherwise.
     */
    public boolean assignClassroomToCourse(String courseID, String classroomID) {
        String sql = "INSERT OR IGNORE INTO Course_Classroom (CourseID, ClassroomID) VALUES (?, ?);";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courseID);
            pstmt.setString(2, classroomID);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Assigned Classroom: " + classroomID + " to Course: " + courseID);
                return true;
            } else {
                System.out.println("Assignment already exists for Classroom: " + classroomID + " and Course: " + courseID);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error assigning Classroom: " + classroomID + " to Course: " + courseID);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the assigned classroom for a specific course.
     *
     * @param courseID The CourseID.
     * @return The ClassroomID if assigned, null otherwise.
     */
    public String getAssignedClassroom(String courseID) {
        String sql = "SELECT ClassroomID FROM Course_Classroom WHERE CourseID = ?;";
        String classroomID = null;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courseID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                classroomID = rs.getString("ClassroomID");
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving assigned classroom for Course: " + courseID);
            e.printStackTrace();
        }

        return classroomID;
    }

    /**
     * Retrieves all assignments of courses to classrooms.
     *
     * @return A list of String arrays where each array contains CourseID and ClassroomID.
     */
    public List<String[]> getAllAssignments() {
        List<String[]> assignments = new ArrayList<>();
        String sql = "SELECT CourseID, ClassroomID FROM Course_Classroom ORDER BY CourseID;";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                assignments.add(new String[]{rs.getString("CourseID"), rs.getString("ClassroomID")});
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving assignments.");
            e.printStackTrace();
        }

        return assignments;
    }

    /**
     * Retrieves all classrooms that have not been assigned yet.
     *
     * @return A list of available ClassroomIDs.
     */
    public List<String> getAvailableClassrooms() {
        List<String> availableClassrooms = new ArrayList<>();
        String sql = "SELECT ClassroomID FROM Classrooms WHERE ClassroomID NOT IN (SELECT ClassroomID FROM Course_Classroom) ORDER BY Capacity DESC;";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                availableClassrooms.add(rs.getString("ClassroomID"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving available classrooms.");
            e.printStackTrace();
        }

        return availableClassrooms;
    }

    /**
     * Retrieves all courses that have not been assigned a classroom yet.
     *
     * @return A list of unassigned CourseIDs.
     */
    public List<String> getUnassignedCourses() {
        List<String> unassignedCourses = new ArrayList<>();
        String sql = "SELECT CourseID FROM Courses WHERE CourseID NOT IN (SELECT CourseID FROM Course_Classroom) ORDER BY CourseID;";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                unassignedCourses.add(rs.getString("CourseID"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving unassigned courses.");
            e.printStackTrace();
        }

        return unassignedCourses;
    }



}
