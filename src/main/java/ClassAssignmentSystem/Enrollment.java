package ClassAssignmentSystem;

//!?????!!!
public class Enrollment {
    private String studentName;
    private String courseCode;

    public Enrollment(String courseCode, String studentName) {
        this.courseCode = courseCode;
        this.studentName = studentName;
    }

    public String getStudent() {
        return studentName;
    }

    public String getCourse() {
        return courseCode;
    }

    public void createEnrollment(Student student, Course course) {

    }
}
