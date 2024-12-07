package ClassAssignmentSystem;

import java.util.ArrayList;
import java.util.List;

public class Course {
    private final String code;
    private final String timeToStart;
    private final int durationInLectureHours;
    private final String lecturer;
    private List<Student> enrolledStudents;
    private Classroom assignedClassroom;
    private int studentCount; // New field to store the number of enrolled students

    public Course(String courseID, String timeToStart, int durationInLectureHours, String lecturer, Classroom assignedClassroom) {
        this.code = courseID;
        this.timeToStart = timeToStart;
        this.durationInLectureHours = durationInLectureHours;
        this.lecturer = lecturer;
        this.assignedClassroom = assignedClassroom;
        this.enrolledStudents = new ArrayList<>();
        this.studentCount = 0; // Initialize to zero
    }

    // Existing getters...

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int count) {
        this.studentCount = count;
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

    public Classroom getAssignedClassroom() {
        return assignedClassroom;
    }

    // Optional: Setters if you plan to modify course details in the future
}
