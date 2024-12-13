package ClassAssignmentSystem;

import javafx.scene.control.CheckBox;

import java.util.List;

public class Student {

    private final String name;
    private List<Course> enrolledCourses;
    private final CheckBox selectBox;

    public Student(String studentName, CheckBox selectBox) {
        this.name = studentName;
        this.selectBox = selectBox;
    }

    public String getName() {
        return name;
    }

    public CheckBox getSelectBox() {
        return selectBox;
    }
}
