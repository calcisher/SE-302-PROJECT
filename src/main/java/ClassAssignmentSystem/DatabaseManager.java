package ClassAssignmentSystem;
import java.util.List;
import java.sql.*;

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

    //For adding Classroom CSV data to Courses Table.
    public void insertClassroomData(String tableName, String[] columnNames, List<String[]> data) throws SQLException {
        if (data.isEmpty()) return;

        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        for (String column : columnNames) {
            sql.append("\"").append(column).append("\",");
        }
        sql.deleteCharAt(sql.length() - 1).append(") VALUES (");
        sql.append("?,".repeat(columnNames.length));
        sql.deleteCharAt(sql.length() - 1).append(")");

        //For test and debugging purposes
        System.out.println("Generated SQL: " + sql);

        try (Connection conn = getConnection();
             PreparedStatement classroomsStmt = conn.prepareStatement(sql.toString());
             PreparedStatement checkExistStmt = conn.prepareStatement("SELECT COUNT(*) FROM " + tableName + " WHERE Classroom = ? AND Capacity = ?")) {

            for (String[] row : data) {  // Skip the header row
                //Check row length matches the number of columns ("Classroom","Capacity")
                if (row.length != columnNames.length) {
                    System.err.println("Warning: Skipping row with mismatched columns.");
                    continue;
                }

                //Check if the data already exists
                checkExistStmt.setString(1, row[0]); // Classroom
                checkExistStmt.setString(2, row[1]); // Capacity

                ResultSet rs = checkExistStmt.executeQuery();
                rs.next();
                int count = rs.getInt(1);

                if (count == 0) {
                    for (int colIndex = 0; colIndex < row.length; colIndex++) {
                        classroomsStmt.setString(colIndex + 1, row[colIndex]);
                    }
                    classroomsStmt.executeUpdate();
                } else {
                    //For Test
                    System.out.println("Skipping duplicate entry for classroom: " + row[0]);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
            throw e;
        }
    }

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
            //throw new RuntimeException(e);
            System.out.println("Illegal argument.");
        }

        return null;
    }

}