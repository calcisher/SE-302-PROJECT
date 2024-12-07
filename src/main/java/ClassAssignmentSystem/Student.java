package ClassAssignmentSystem;

import java.util.List;

public class Student {

    private final String name;
    private List<Course> enrolledCourses;

    public Student(String studentName) {
        this.name = studentName;
    }

    public String getName() {
        return name;
    }
}
