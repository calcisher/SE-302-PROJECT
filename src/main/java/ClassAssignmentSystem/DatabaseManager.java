package ClassAssignmentSystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;


import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class DatabaseManager {

    private static String databasePath;

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);




    // Store classroom schedules in-memory to track assignments during the operation
    private Map<String, List<Schedule>> Schedules;

    public DatabaseManager(String databasePath) {
        DatabaseManager.databasePath = databasePath;
        Schedules = new HashMap<>();
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("Connected to the database.");

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ObservableList<ScheduleController.ScheduleEntry> getStudentSchedule(String studentName) {
        ObservableList<ScheduleController.ScheduleEntry> scheduleData = FXCollections.observableArrayList();

        String query = "SELECT TimeToStart, Course, Classroom, DurationInLectureHours FROM Courses WHERE Students = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, studentName);
            ResultSet resultSet = preparedStatement.executeQuery();

            Map<String, Map<LocalTime, String>> weeklySchedule = new HashMap<>();

            String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

            for (String day : days) {
                weeklySchedule.put(day, new TreeMap<>());
            }

            while (resultSet.next()) {
                String timeToStart = resultSet.getString("TimeToStart");
                String course = resultSet.getString("Course");
                String classroom = resultSet.getString("Classroom");
                int duration = resultSet.getInt("DurationInLectureHours");


                String[] parts = timeToStart.split(" ");
                if (parts.length == 2) {
                    String day = parts[0];
                    String time = parts[1];

                    try {
                        LocalTime startTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("H:mm"));

                        String courseAndClassroom = course + " \n(" + classroom + ")";

                        if (weeklySchedule.containsKey(day)) {
                            for (int i = 0; i < duration; i++) {
                                LocalTime slotTime = startTime.plusMinutes(i * 55);
                                weeklySchedule.get(day).put(slotTime, courseAndClassroom);
                            }
                        }
                    } catch (DateTimeParseException e) {
                        System.err.println("Invalıd Time For: " + time);
                    }
                }
            }

            LocalTime currentTime = LocalTime.of(8, 30); // 08:30 start time
            LocalTime endTime = LocalTime.of(22, 15); // 22:15 end time

            while (!currentTime.isAfter(endTime)) {
                String time = String.format("%02d:%02d", currentTime.getHour(), currentTime.getMinute());

                String monday = weeklySchedule.get("Monday").getOrDefault(currentTime, "");
                String tuesday = weeklySchedule.get("Tuesday").getOrDefault(currentTime, "");
                String wednesday = weeklySchedule.get("Wednesday").getOrDefault(currentTime, "");
                String thursday = weeklySchedule.get("Thursday").getOrDefault(currentTime, "");
                String friday = weeklySchedule.get("Friday").getOrDefault(currentTime, "");

                scheduleData.add(new ScheduleController.ScheduleEntry(
                        time,
                        monday,
                        tuesday,
                        wednesday,
                        thursday,
                        friday
                ));

                currentTime = currentTime.plusMinutes(55);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return scheduleData;
    }


    public static ObservableList<ScheduleController.ScheduleEntry> getClassSchedule(String className) {
        ObservableList<ScheduleController.ScheduleEntry> scheduleData = FXCollections.observableArrayList();

        String query = "SELECT TimeToStart, Course, Classroom, DurationInLectureHours FROM Courses WHERE Classroom = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, className);
            ResultSet resultSet = preparedStatement.executeQuery();

            Map<String, Map<LocalTime, String>> weeklySchedule = new HashMap<>();

            String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

            for (String day : days) {
                weeklySchedule.put(day, new TreeMap<>());
            }

            while (resultSet.next()) {
                String timeToStart = resultSet.getString("TimeToStart");
                String course = resultSet.getString("Course");
                int duration = resultSet.getInt("DurationInLectureHours");


                String[] parts = timeToStart.split(" ");
                if (parts.length == 2) {
                    String day = parts[0];
                    String time = parts[1];

                    try {
                        LocalTime startTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("H:mm"));

                        if (weeklySchedule.containsKey(day)) {
                            for (int i = 0; i < duration; i++) {
                                LocalTime slotTime = startTime.plusMinutes(i * 55);
                                weeklySchedule.get(day).put(slotTime, course);
                            }
                        }
                    } catch (DateTimeParseException e) {
                        System.err.println("Invalid Time For" + time);
                    }
                }
            }

            LocalTime currentTime = LocalTime.of(8, 30); // 08:30 start time
            LocalTime endTime = LocalTime.of(22, 15); // 22:15 end time

            while (!currentTime.isAfter(endTime)) {
                String time = String.format("%02d:%02d", currentTime.getHour(), currentTime.getMinute());

                String monday = weeklySchedule.get("Monday").getOrDefault(currentTime, "");
                String tuesday = weeklySchedule.get("Tuesday").getOrDefault(currentTime, "");
                String wednesday = weeklySchedule.get("Wednesday").getOrDefault(currentTime, "");
                String thursday = weeklySchedule.get("Thursday").getOrDefault(currentTime, "");
                String friday = weeklySchedule.get("Friday").getOrDefault(currentTime, "");

                scheduleData.add(new ScheduleController.ScheduleEntry(
                        time,
                        monday,
                        tuesday,
                        wednesday,
                        thursday,
                        friday
                ));

                currentTime = currentTime.plusMinutes(55);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return scheduleData;
    }



    public static ObservableList<ScheduleController.ScheduleEntry> getFreeTimeSchedule(List<Student> selectedStudents) {
        ObservableList<ScheduleController.ScheduleEntry> scheduleData = FXCollections.observableArrayList();

        try (Connection connection = getConnection()) {
            // Initialize a map to track common free time slots for all selected students
            Map<String, Map<LocalTime, Boolean>> commonFreeTimeSlots = new HashMap<>();
            String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

            // Initialize the schedule with all time slots marked as free
            for (String day : days) {
                Map<LocalTime, Boolean> dailySchedule = new TreeMap<>();
                LocalTime currentTime = LocalTime.of(8, 30); // Start time
                LocalTime endTime = LocalTime.of(22, 15);    // End time

                while (!currentTime.isAfter(endTime)) {
                    dailySchedule.put(currentTime, true); // All slots are free initially
                    currentTime = currentTime.plusMinutes(55); // Slot interval
                }
                commonFreeTimeSlots.put(day, dailySchedule);
            }

            // Process each student's schedule to mark busy time slots
            for (Student student : selectedStudents) {
                String query = "SELECT TimeToStart, DurationInLectureHours FROM Courses WHERE Students = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, student.getName());
                    ResultSet resultSet = preparedStatement.executeQuery();

                    while (resultSet.next()) {
                        String timeToStart = resultSet.getString("TimeToStart");
                        int duration = resultSet.getInt("DurationInLectureHours");
                        String[] parts = timeToStart.split(" ");
                        if (parts.length == 2) {
                            String day = parts[0];
                            String time = parts[1];

                            try {
                                LocalTime startTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("H:mm"));
                                if (commonFreeTimeSlots.containsKey(day)) {
                                    for (int i = 0; i < duration; i++) {
                                        LocalTime slotTime = startTime.plusMinutes(i * 55);
                                        commonFreeTimeSlots.get(day).put(slotTime, false); // Mark as busy
                                    }
                                }
                            } catch (DateTimeParseException e) {
                                System.err.println("Invalid time format: " + time);
                            }
                        }
                    }
                }
            }

            // Generate the final schedule with common free slots
            LocalTime currentTime = LocalTime.of(8, 30);
            LocalTime endTime = LocalTime.of(22, 15);

            while (!currentTime.isAfter(endTime)) {
                String time = String.format("%02d:%02d", currentTime.getHour(), currentTime.getMinute());

                String monday = isSlotFree(commonFreeTimeSlots.get("Monday"), currentTime) ? "Free" : "";
                String tuesday = isSlotFree(commonFreeTimeSlots.get("Tuesday"), currentTime) ? "Free" : "";
                String wednesday = isSlotFree(commonFreeTimeSlots.get("Wednesday"), currentTime) ? "Free" : "";
                String thursday = isSlotFree(commonFreeTimeSlots.get("Thursday"), currentTime) ? "Free" : "";
                String friday = isSlotFree(commonFreeTimeSlots.get("Friday"), currentTime) ? "Free" : "";

                scheduleData.add(new ScheduleController.ScheduleEntry(
                        time,
                        monday,
                        tuesday,
                        wednesday,
                        thursday,
                        friday
                ));

                currentTime = currentTime.plusMinutes(55);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return scheduleData;
    }

    private static boolean isSlotFree(Map<LocalTime, Boolean> dailySchedule, LocalTime timeSlot) {
        return dailySchedule != null && dailySchedule.getOrDefault(timeSlot, false);
    }


    public static ObservableList<String> getDistinctStudentNames() {
        ObservableList<String> studentNames = FXCollections.observableArrayList();
        String query = "SELECT DISTINCT Students FROM Courses";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                studentNames.add(resultSet.getString("Students"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return studentNames;
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

    public void deleteDatabase() {
        try {
            // Create a File object for the database
            File dbFile = new File(databasePath);

            // Check if the database file exists
            if (dbFile.exists()) {
                // Attempt to delete the file
                boolean deleted = dbFile.delete();

                if (deleted) {
                    logger.info("Database '" + databasePath + "' deleted successfully.");
                } else {
                    logger.error("Failed to delete database '" + databasePath + "'.");
                    throw new RuntimeException("Failed to delete database '" + databasePath + "'.");
                }
            } else {
                logger.warn("Database file '" + databasePath + "' does not exist.");
                throw new RuntimeException("Database file '" + databasePath + "' does not exist.");
            }
        } catch (Exception e) {
            logger.error("Error deleting database: ", e);
            throw new RuntimeException("Error deleting database", e);
        }
    }


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

        // Ensure 'Classroom' is included in the columns
        List<String> columnsList = new ArrayList<>(Arrays.asList(columnNames));
        if (!columnsList.contains("Classroom")) {
            columnsList.add("Classroom");
        }

        StringBuilder insertSql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        for (String column : columnsList) {
            insertSql.append("\"").append(column).append("\",");
        }
        insertSql.deleteCharAt(insertSql.length() - 1).append(") VALUES (");
        insertSql.append("?,".repeat(columnsList.size()));
        insertSql.deleteCharAt(insertSql.length() - 1).append(")");

        // Define the check existence SQL
        // Adjust the WHERE clause based on unique identifiers for your Classroom table
        StringBuilder checkExistSql = new StringBuilder("SELECT COUNT(*) FROM ").append(tableName).append(" WHERE Classroom = ?");

        // For test and debugging purposes
        System.out.println("Generated Insert SQL: " + insertSql);
        System.out.println("Generated CheckExist SQL: " + checkExistSql);

        try (Connection conn = getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(insertSql.toString());
             PreparedStatement checkExistStmt = conn.prepareStatement(checkExistSql.toString())) {

            for (String[] row : data) {
                // Ensure the row has the expected number of columns
                if (row.length != columnNames.length) {
                    System.err.println("Warning: Skipping row with mismatched columns.");
                    continue;
                }

                String classroomValue = row[getColumnIndex(columnNames, "Classroom")].trim();
                if (classroomValue.isEmpty()) {
                    System.err.println("Warning: Skipping row with empty Classroom value.");
                    continue;
                }

                // Check if the classroom already exists
                checkExistStmt.setString(1, classroomValue);
                ResultSet rs = checkExistStmt.executeQuery();
                rs.next();
                int count = rs.getInt(1);
                rs.close();

                if (count == 0) {
                    // If the entry doesn't exist, insert the new data
                    for (int i = 0; i < columnNames.length; i++) {
                        insertStmt.setString(i + 1, row[i].trim());
                    }

                    try {
                        insertStmt.executeUpdate();
                        System.out.println("Inserted Classroom: " + classroomValue);
                    } catch (SQLException e) {
                        System.err.println("Error inserting into Classrooms table: " + e.getMessage());
                    }
                } else {
                    // For testing
                    System.out.println("Skipping duplicate Classroom entry: " + classroomValue);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Helper method to find the index of a column name in the columnNames array.
     */
    private int getColumnIndex(String[] columnNames, String columnName) {
        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i].equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Column " + columnName + " not found in columnNames array.");
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
                Schedule courseSchedule = parseSchedule(course.timeToStart, course.durationInLectureHours);
                if (courseSchedule == null) {
                    System.err.println("Invalid schedule for course: " + course.courseCode);
                    continue;
                }

                List<Schedule> existingSchedules = Schedules.getOrDefault(room.getName(), new ArrayList<>());
                boolean conflict = existingSchedules.stream().anyMatch(existing -> existing.overlapsWith(courseSchedule));

                if (!conflict) {
                    // Assign classroom to course
                    assignClassroomToCourse(course.courseCode, room.getName());
                    // Update in-memory schedule
                    existingSchedules.add(courseSchedule);
                    Schedules.put(room.getName(), existingSchedules);
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


    public Map<String, Integer> getCourseStudentCounts() throws SQLException {
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
        String query = "SELECT DISTINCT Classroom, Capacity FROM Classrooms";
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

                    // Retrieve and set student count
                    int studentCount = getCourseStudentCounts().getOrDefault(courseCode, 0);

                    Course courseObj = new Course(courseCode, timeToStart, duration, lecturer, assignedClassroom);
                    courseObj.setStudentCount(studentCount);
                    return courseObj;
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

                Schedule schedule = parseSchedule(timeToStart, duration);
                if (schedule != null) {
                    Schedules
                            .computeIfAbsent(classroom, k -> new ArrayList<>())
                            .add(schedule);
                }
            }
        }
    }


    private Schedule parseSchedule(String timeToStart, int durationInLectureHours) {
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

            return new Schedule(day, startTime, endTime);
        } catch (Exception e) {
            System.err.println("Error parsing course schedule: " + e.getMessage());
            return null;
        }
    }
    public boolean removeStudentFromCourse(String courseCode,String studentName){
        try (Connection conn=getConnection()) {
            String query = "DELETE FROM Courses WHERE Course = ? AND Students = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, courseCode);
                stmt.setString(2, studentName);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addStudentToCourse(String courseCode, String studentNames) {
        try (Connection conn = getConnection()) {
            // Kursun mevcut bilgilerini alıyoruz
            String getCourseDetailsQuery = "SELECT TimeToStart, DurationInLectureHours, Classroom FROM Courses WHERE Course = ?";

            try (PreparedStatement stmt = conn.prepareStatement(getCourseDetailsQuery)) {
                stmt.setString(1, courseCode);
                ResultSet rs = stmt.executeQuery();

                // Eğer kurs bulunamazsa, false döndürüyoruz
                if (!rs.next()) {
                    return false;
                }

                String timeToStart = rs.getString("TimeToStart");
                int durationInLectureHours = rs.getInt("DurationInLectureHours");
                String classroom = rs.getString("Classroom");

                // Öğrencileri virgülle ayırarak alıyoruz
                String[] studentArray = studentNames.split(",");

                // Öğrencileri ayrı ayrı ekliyoruz
                String insertStudentQuery = "INSERT INTO Courses (Course, Students, TimeToStart, DurationInLectureHours, Classroom) VALUES (?, ?, ?, ?, ?)";

                try (PreparedStatement insertStmt = conn.prepareStatement(insertStudentQuery)) {
                    for (String studentName : studentArray) {
                        studentName = studentName.trim(); // Öğrenci adlarını temizliyoruz, boşlukları kaldırıyoruz
                        insertStmt.setString(1, courseCode);
                        insertStmt.setString(2, studentName);
                        insertStmt.setString(3, timeToStart);
                        insertStmt.setInt(4, durationInLectureHours);
                        insertStmt.setString(5, classroom);

                        // Her öğrenci için sorguyu çalıştırıyoruz
                        insertStmt.addBatch();
                    }

                    // Batch işlemi ile veritabanına toplu ekleme yapıyoruz
                    int[] results = insertStmt.executeBatch();

                    // Eğer işlem başarılıysa, her öğrenci için pozitif bir değer döner
                    for (int result : results) {
                        if (result <= 0) {
                            return false;  // Eğer bir öğrenci eklenemediyse, işlemi başarısız kabul ediyoruz
                        }
                    }

                    return true;  // Eğer tüm öğrenciler başarıyla eklenmişse
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static ObservableList<String> getStudentsNotInCourse(String courseCode) {
        ObservableList<String> studentNames = FXCollections.observableArrayList();
        String query = "SELECT DISTINCT Students FROM Courses WHERE Students NOT IN (SELECT Students FROM Courses WHERE Course = ?)";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, courseCode);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    studentNames.add(resultSet.getString("Students"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return studentNames;
    }

    public int getCourseCapacity(String courseCode) {
        String query = "SELECT class.Capacity FROM Classrooms class, Courses course WHERE class.Classroom = course.Classroom AND course.Course = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, courseCode);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("Capacity");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Default to 0 if no data found
    }

    public int getCourseStudentCount(String courseCode) {
        String query = "SELECT COUNT(*) AS StudentCount FROM Courses WHERE Course = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, courseCode);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("StudentCount");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Default to 0 if no data found
    }

    public int getRemainingCapacity(String courseName) {
        try (Connection connection = getConnection()) {
            String query = "SELECT class.Capacity - COUNT(*) AS Capacity " +
                    "FROM Courses course, Classrooms class " +
                    "WHERE course.Classroom = class.Classroom AND Course = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, courseName);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("Capacity");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Eğer bir hata varsa veya sonuç boşsa, kalan kapasite 0 olarak döndürülür
    }



    public Classroom findAvailableClass(String day, String startHour, int duration) throws SQLException {
        List<Classroom> classrooms = getAllClassroomsWithCapacity();

        for (Classroom classroom : classrooms) {
            List<Schedule> schedules = Schedules.getOrDefault(classroom.getName(), new ArrayList<>());

            Schedule newSchedule = parseSchedule(day + " " + startHour, duration);
            boolean conflict = schedules.stream().anyMatch(schedule -> schedule.overlapsWith(newSchedule));

            if (!conflict) {
                return classroom;
            }
        }
        return null; // No available classrooms
    }

    public void insertNewCourse(String courseID, String timeToStart, int duration, String lecturer, List<Student> students, Classroom classroom) throws SQLException {
        String insertCourseSQL = "INSERT INTO Courses (Course, TimeToStart, DurationInLectureHours, Lecturer, Students, Classroom) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertCourseSQL)) {

            for (Student student : students) {
                pstmt.setString(1, courseID);
                pstmt.setString(2, timeToStart);
                pstmt.setInt(3, duration);
                pstmt.setString(4, lecturer);
                pstmt.setString(5, student.getName());
                pstmt.setString(6, classroom.getName());
                pstmt.executeUpdate();
            }
        }
    }
    // Method to get common free times for selected students on a specific day
    public static ObservableList<String> getCommonFreeTimes(List<Student> selectedStudents, String day) throws SQLException {
        // Initialize a map to track free times for each student
        Map<String, Set<String>> studentFreeTimesMap = new HashMap<>();

        for (Student student : selectedStudents) {
            Set<String> freeTimes = getFreeTimesForStudentOnDay(student.getName(), day);
            studentFreeTimesMap.put(student.getName(), freeTimes);
        }

        // Find the intersection of free times across all selected students
        Set<String> commonFreeTimes = new HashSet<>();
        boolean first = true;

        for (Set<String> freeTimes : studentFreeTimesMap.values()) {
            if (first) {
                commonFreeTimes.addAll(freeTimes);
                first = false;
            } else {
                commonFreeTimes.retainAll(freeTimes);
            }
        }

        // Sort the free times
        List<String> sortedFreeTimes = new ArrayList<>(commonFreeTimes);
        sortedFreeTimes.sort(Comparator.comparing(time -> LocalTime.parse(time, DateTimeFormatter.ofPattern("H:mm"))));

        return FXCollections.observableArrayList(sortedFreeTimes);
    }

    // Helper method to get free times for a single student on a specific day
    private static Set<String> getFreeTimesForStudentOnDay(String studentName, String day) throws SQLException {
        Set<String> busyTimes = new HashSet<>();
        String query = "SELECT TimeToStart, DurationInLectureHours FROM Courses WHERE Students = ? AND TimeToStart LIKE ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, studentName);
            preparedStatement.setString(2, day + "%"); // Matches the day

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String timeToStart = resultSet.getString("TimeToStart");
                int duration = resultSet.getInt("DurationInLectureHours");
                String[] parts = timeToStart.split(" ");
                if (parts.length == 2) {
                    String time = parts[1];
                    LocalTime startTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("H:mm"));
                    for (int i = 0; i < duration; i++) {
                        LocalTime slotTime = startTime.plusMinutes(i * 55); // Assuming 55-minute slots
                        busyTimes.add(slotTime.format(DateTimeFormatter.ofPattern("H:mm")));
                    }
                }
            }
        }

        // Define all possible time slots
        Set<String> allTimeSlots = new HashSet<>();
        LocalTime currentTime = LocalTime.of(8, 30); // 08:30 start time
        LocalTime endTime = LocalTime.of(22, 15); // 22:15 end time

        while (!currentTime.isAfter(endTime)) {
            allTimeSlots.add(currentTime.format(DateTimeFormatter.ofPattern("H:mm")));
            currentTime = currentTime.plusMinutes(55);
        }

        // Free times are all time slots minus busy times
        allTimeSlots.removeAll(busyTimes);

        return allTimeSlots;
    }

    // Method to determine the maximum number of continuous free slots starting from a specific time
    public static int getMaxContinuousFreeSlots(List<Student> selectedStudents, String day, LocalTime startTime) throws SQLException {
        // Fetch all common free times for the selected day
        ObservableList<String> commonFreeTimes = getCommonFreeTimes(selectedStudents, day);

        // Convert to sorted list
        List<LocalTime> sortedFreeTimes = new ArrayList<>();
        for (String timeStr : commonFreeTimes) {
            sortedFreeTimes.add(LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("H:mm")));
        }
        sortedFreeTimes.sort(Comparator.naturalOrder());

        // Find the index of the start time
        int index = sortedFreeTimes.indexOf(startTime);
        if (index == -1) {
            return 0;
        }

        // Count continuous free slots
        int count = 0;
        for (int i = index; i < sortedFreeTimes.size() - 1; i++) {
            LocalTime current = sortedFreeTimes.get(i);
            LocalTime next = sortedFreeTimes.get(i + 1);
            if (current.plusMinutes(55).equals(next)) {
                count++;
            } else {
                break;
            }
        }

        return count + 1; // Including the initial slot
    }



    public List<Classroom> getAvailableClassroomsForCourse(Course course) throws SQLException {
        int studentCount = course.getStudentCount();
        List<Classroom> suitableClassrooms = getAllClassroomsWithCapacity().stream()
                .filter(classroom -> classroom.getCapacity() >= studentCount)
                .collect(Collectors.toList());

        List<Classroom> availableClassrooms = new ArrayList<>();

        Schedule courseSchedule = parseSchedule(course.getTimeToStart(), course.getDurationInLectureHours());
        if (courseSchedule == null) {
            return availableClassrooms; // Empty list if schedule is invalid
        }

        for (Classroom classroom : suitableClassrooms) {
            List<Schedule> existingSchedules = Schedules.getOrDefault(classroom.getName(), new ArrayList<>());
            boolean conflict = existingSchedules.stream().anyMatch(existing -> existing.overlapsWith(courseSchedule));
            if (!conflict) {
                availableClassrooms.add(classroom);
            }
        }

        return availableClassrooms.stream().distinct().collect(Collectors.toList());
    }


    public void updateCourseClassroom(String courseCode, String newClassroomName) throws SQLException {
        String updateSQL = "UPDATE Courses SET Classroom = ? WHERE Course = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
            pstmt.setString(1, newClassroomName);
            pstmt.setString(2, courseCode);
            pstmt.executeUpdate();
        }
    }

    public void updateSchedulesAfterClassroomChange(Course course, String newClassroomName) throws SQLException {
        // Remove schedule from old classroom
        String oldClassroom = course.getAssignedClassroom() != null ? course.getAssignedClassroom().getName() : null;
        if (oldClassroom != null) {
            Schedule courseSchedule = parseSchedule(course.getTimeToStart(), course.getDurationInLectureHours());
            if (courseSchedule != null) {
                List<Schedule> oldSchedules = Schedules.getOrDefault(oldClassroom, new ArrayList<>());
                oldSchedules.removeIf(existing -> existing.equals(courseSchedule));
                Schedules.put(oldClassroom, oldSchedules);
            }
        }

        // Add schedule to new classroom
        Schedule newSchedule = parseSchedule(course.getTimeToStart(), course.getDurationInLectureHours());
        if (newSchedule != null) {
            List<Schedule> newSchedules = Schedules.getOrDefault(newClassroomName, new ArrayList<>());
            newSchedules.add(newSchedule);
            Schedules.put(newClassroomName, newSchedules);
        }

        // Update the Course object's assigned classroom
        course.setAssignedClassroom(getClassroomDetails(newClassroomName));
    }
}
