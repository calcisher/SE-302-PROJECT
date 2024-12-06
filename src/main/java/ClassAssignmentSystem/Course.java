package ClassAssignmentSystem;

public class Course {
    private String courseID;
    private String timeToStart;
    private int durationInLectureHours;
    private String lecturer;

    public Course(String courseID, String timeToStart, int durationInLectureHours, String lecturer) {
        this.courseID = courseID;
        this.timeToStart = timeToStart;
        this.durationInLectureHours = durationInLectureHours;
        this.lecturer = lecturer;
    }

    // Getters
    public String getCourseID() {
        return courseID;
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
