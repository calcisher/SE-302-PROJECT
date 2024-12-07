package ClassAssignmentSystem;

import java.util.List;

public class Course {
    private final String code;
    private final String timeToStart;
    private final int durationInLectureHours;
    private final String lecturer;
    private List<Student> enrolledStudents;


    public Course(String courseID, String timeToStart, int durationInLectureHours, String lecturer) {
        this.code = courseID;
        this.timeToStart = timeToStart;
        this.durationInLectureHours = durationInLectureHours;
        this.lecturer = lecturer;
    }

    // Getters
    public String getCode() {
        return code;
    }

    public String getTimeToStart() {
        return timeToStart;
    }

    public int getDurationInLectureHours() {
        return durationInLectureHours;
    }

    public String getLecturer() {
        return lecturer;
    }

    // Optional: Setters if you plan to modify course details in the future
}
