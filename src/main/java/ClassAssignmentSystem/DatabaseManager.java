package ClassAssignmentSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DatabaseManager {

    private static String databasePath;

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);


    // Store classroom schedules in-memory to track assignments during the operation
    private Map<String, List<CourseSchedule>> classroomSchedules;

    public DatabaseManager(String databasePath) {
        DatabaseManager.databasePath = databasePath;
        classroomSchedules = new HashMap<>();
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

    public void createTable(String tableName, Map<String, String> columns) throws SQLException {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
        for (Map.Entry<String, String> entry : columns.entrySet()) {
            sql.append("\"").append(entry.getKey()).append("\" ").append(entry.getValue()).append(",");
        }
        sql.deleteCharAt(sql.length() - 1).append(")");
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql.toString());
        }
    }


    public void initializeDatabaseAfterImport() {
        try (Connection conn = getConnection()) {
            addClassroomColumnIfMissing();
            loadExistingSchedules(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle or propagate the exception as needed
        }
    }


    public void addClassroomColumnIfMissing() throws SQLException {
        String checkColumnQuery = "PRAGMA table_info(Courses);";
        boolean columnExists = false;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkColumnQuery)) {
            while (rs.next()) {
                String columnName = rs.getString("name");
                if ("Classroom".equalsIgnoreCase(columnName)) {
                    columnExists = true;
                    break;
                }
            }
        }

        if (!columnExists) {
            String alterTableSQL = "ALTER TABLE Courses ADD COLUMN Classroom TEXT;";
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute(alterTableSQL);
                System.out.println("Added 'Classroom' column to 'Courses' table.");
            } catch (SQLException e) {
                System.err.println("Error adding 'Classroom' column: " + e.getMessage());
                throw e;
            }
        } else {
            System.out.println("'Classroom' column already exists in 'Courses' table.");
        }
    }



    public void deleteTable(String tableName) throws SQLException {
        String sql = "DROP TABLE IF EXISTS \"" + tableName + "\"";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void deleteDatabase() {}

    //For adding Course CSV data to Courses Table.
    public void insertCourseData(String coursesTableName, String[] columnNames, List<String[]> data) throws SQLException {
        if (data.isEmpty()) return;

        // Ensure 'Classroom' is included in the columns
        List<String> columnsList = new ArrayList<>(Arrays.asList(columnNames));
        if (!columnsList.contains("Classroom")) {
            columnsList.add("Classroom");
        }

        StringBuilder coursesSql = new StringBuilder("INSERT INTO ").append(coursesTableName).append(" (");
        for (String column : columnsList) {
            coursesSql.append("\"").append(column).append("\",");
        }
        coursesSql.deleteCharAt(coursesSql.length() - 1).append(") VALUES (");
        coursesSql.append("?,".repeat(columnsList.size()));
        coursesSql.deleteCharAt(coursesSql.length() - 1).append(")");

        // For test and debugging purposes
        System.out.println("Generated SQL: " + coursesSql);

        try (Connection conn = getConnection();
             PreparedStatement coursesStmt = conn.prepareStatement(coursesSql.toString());
             PreparedStatement checkExistStmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM " + coursesTableName + " WHERE Course = ? AND TimeToStart = ? AND Lecturer = ? AND Students = ?")) {

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
                            coursesStmt.setInt(3, Integer.parseInt(row[2])); // DurationInLectureHours
                            coursesStmt.setString(4, row[3]); // Lecturer
                            coursesStmt.setString(5, student); // Student
                            coursesStmt.setNull(6, Types.VARCHAR); // Classroom set to NULL

                            try {
                                coursesStmt.executeUpdate();
                            } catch (SQLException e) {
                                System.err.println("Error inserting into Courses table: " + e.getMessage());
                            }
                        } else {
                            // For test
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


    // Retrieve all courses
    public List<String> getAllCourses() throws SQLException {
        List<String> courses = new ArrayList<>();
        String query = "SELECT DISTINCT Course FROM Courses";
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
    /**
     * Assigns courses to classrooms based on capacity and time availability.
     * @return true if all courses are assigned successfully, false otherwise.
     * @throws SQLException
     */
    public boolean assignCoursesToClassrooms() throws SQLException {
        // Step 1: Retrieve all distinct courses with their student counts
        Map<String, Integer> courseStudentCounts = getCourseStudentCounts();
        List<CourseAssignment> unassignedCourses = getUnassignedCourses(courseStudentCounts.keySet());

        if (unassignedCourses.isEmpty()) {
            System.out.println("No unassigned courses found.");
            return true;
        }

        // Step 2: Retrieve all classrooms with their capacities
        List<Classroom> classrooms = getAllClassroomsWithCapacity();

        if (classrooms.isEmpty()) {
            System.err.println("No classrooms available for assignment.");
            return false;
        }

        // Step 3: Sort classrooms by capacity ascending to fit smallest possible first
        classrooms.sort(Comparator.comparingInt(Classroom::getCapacity));

        boolean allAssigned = true;

        for (CourseAssignment course : unassignedCourses) {
            int studentCount = courseStudentCounts.getOrDefault(course.courseCode, 0);
            System.out.println("Assigning course: " + course.courseCode + " with " + studentCount + " students.");

            // Step 4: Find eligible classrooms based on capacity
            List<Classroom> eligibleClassrooms = classrooms.stream()
                    .filter(room -> room.getCapacity() >= studentCount)
                    .collect(Collectors.toList());

            if (eligibleClassrooms.isEmpty()) {
                System.err.println("No classrooms can accommodate course: " + course.courseCode);
                allAssigned = false;
                continue;
            }

            // Step 5: Attempt to assign to an eligible classroom without time conflicts
            boolean assigned = false;
            for (Classroom room : eligibleClassrooms) {
                CourseSchedule courseSchedule = parseCourseSchedule(course.timeToStart, course.durationInLectureHours);
                if (courseSchedule == null) {
                    System.err.println("Invalid schedule for course: " + course.courseCode);
                    continue;
                }

                List<CourseSchedule> existingSchedules = classroomSchedules.getOrDefault(room.getName(), new ArrayList<>());
                boolean conflict = existingSchedules.stream().anyMatch(existing -> existing.overlapsWith(courseSchedule));

                if (!conflict) {
                    // Assign classroom to course
                    assignClassroomToCourse(course.courseCode, room.getName());
                    // Update in-memory schedule
                    existingSchedules.add(courseSchedule);
                    classroomSchedules.put(room.getName(), existingSchedules);
                    System.out.println("Assigned classroom " + room.getName() + " to course " + course.courseCode);
                    assigned = true;
                    break; // Move to the next course after successful assignment
                } else {
                    System.out.println("Time conflict detected for classroom " + room.getName() + " with course " + course.courseCode);
                }
            }

            if (!assigned) {
                System.err.println("Unable to assign a classroom for course: " + course.courseCode);
                allAssigned = false;
            }
        }

        return allAssigned;
    }


    private Map<String, Integer> getCourseStudentCounts() throws SQLException {
        Map<String, Integer> courseCounts = new HashMap<>();
        String query = "SELECT Course, COUNT(Students) as StudentCount FROM Courses GROUP BY Course";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String course = rs.getString("Course");
                int count = rs.getInt("StudentCount");
                courseCounts.put(course, count);
            }
        }
        return courseCounts;
    }

    private List<CourseAssignment> getUnassignedCourses(Set<String> courseCodes) throws SQLException {
        List<CourseAssignment> unassigned = new ArrayList<>();
        String query = "SELECT DISTINCT Course, TimeToStart, DurationInLectureHours FROM Courses WHERE Classroom IS NULL";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String course = rs.getString("Course");
                String timeToStart = rs.getString("TimeToStart");
                int duration = Integer.parseInt(rs.getString("DurationInLectureHours"));
                unassigned.add(new CourseAssignment(course, timeToStart, duration));
            }
        }
        return unassigned;
    }

    private List<Classroom> getAllClassroomsWithCapacity() throws SQLException {
        List<Classroom> classrooms = new ArrayList<>();
        String query = "SELECT Classroom, Capacity FROM Classrooms";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String name = rs.getString("Classroom");
                int capacity = Integer.parseInt(rs.getString("Capacity"));
                classrooms.add(new Classroom(name, capacity));
            }
        }
        return classrooms;
    }

    private void assignClassroomToCourse(String courseCode, String classroomName) throws SQLException {
        String update = "UPDATE Courses SET Classroom = ? WHERE Course = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(update)) {
            pstmt.setString(1, classroomName);
            pstmt.setString(2, courseCode);
            pstmt.executeUpdate();
        }
    }

    // Retrieve course details
    public Course getCourseDetails(String course) throws SQLException {
        String query = "SELECT Course, TimeToStart, DurationInLectureHours, Lecturer, Classroom " +
                "FROM Courses WHERE Course = ? LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, course);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String courseCode = rs.getString("Course");
                    String timeToStart = rs.getString("TimeToStart");
                    int duration = rs.getInt("DurationInLectureHours");
                    String lecturer = rs.getString("Lecturer");
                    String classroomName = rs.getString("Classroom");

                    // Handle empty strings as NULL
                    if (classroomName != null && classroomName.trim().isEmpty()) {
                        classroomName = null;
                    }

                    Classroom assignedClassroom = (classroomName != null && !classroomName.trim().isEmpty())
                            ? getClassroomDetails(classroomName)
                            : null;

                    //Classroom assignedClassroom = (classroomName != null) ? getClassroomDetails(classroomName) : null;
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
                    return rs.getString("Classroom");
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
    public List<String> getStudentsForCourse(String course) throws SQLException {
        List<String> students = new ArrayList<>();
        String query = "SELECT Students FROM Courses WHERE Course = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, course);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(rs.getString("Students"));
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

    private void loadExistingSchedules(Connection conn) throws SQLException {
        String query = "SELECT Course, TimeToStart, DurationInLectureHours, Classroom FROM Courses WHERE Classroom IS NOT NULL";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String course = rs.getString("Course");
                String timeToStart = rs.getString("TimeToStart");
                int duration = rs.getInt("DurationInLectureHours");
                String classroom = rs.getString("Classroom");

                CourseSchedule schedule = parseCourseSchedule(timeToStart, duration);
                if (schedule != null) {
                    classroomSchedules
                            .computeIfAbsent(classroom, k -> new ArrayList<>())
                            .add(schedule);
                }
            }
        }
    }


    private CourseSchedule parseCourseSchedule(String timeToStart, int durationInLectureHours) {
        try {
            // Assuming timeToStart is in format "Monday 8:30"
            String[] parts = timeToStart.split(" ");
            if (parts.length != 2) {
                System.err.println("Invalid timeToStart format: " + timeToStart);
                return null;
            }

            DayOfWeek day;
            if (parts[0].equals("Friday")) {
                 day = DayOfWeek.FRIDAY;
            }
            else {
                 day = DayOfWeek.valueOf(parts[0].toUpperCase());
            }

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
            LocalTime startTime = LocalTime.parse(parts[1], timeFormatter);

            // Calculate end time based on duration
            // Each lecture hour: 45 minutes lecture + 10 minutes break, except after the last lecture
            int totalMinutes = durationInLectureHours * (45 + 10) - 10; // Remove last break
            LocalTime endTime = startTime.plusMinutes(totalMinutes);

            return new CourseSchedule(day, startTime, endTime);
        } catch (Exception e) {
            System.err.println("Error parsing course schedule: " + e.getMessage());
            return null;
        }
    }


    private static class CourseAssignment {
        String courseCode;
        String timeToStart;
        int durationInLectureHours;

        CourseAssignment(String courseCode, String timeToStart, int durationInLectureHours) {
            this.courseCode = courseCode;
            this.timeToStart = timeToStart;
            this.durationInLectureHours = durationInLectureHours;
        }
    }

    private static class CourseSchedule {
        DayOfWeek day;
        LocalTime startTime;
        LocalTime endTime;

        CourseSchedule(DayOfWeek day, LocalTime startTime, LocalTime endTime) {
            this.day = day;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        /**
         * Checks if this schedule overlaps with another schedule.
         * @param other The other CourseSchedule to compare with.
         * @return true if there is an overlap, false otherwise.
         */
        boolean overlapsWith(CourseSchedule other) {
            if (this.day != other.day) {
                return false;
            }
            return this.startTime.isBefore(other.endTime) && other.startTime.isBefore(this.endTime);
        }
    }


}
