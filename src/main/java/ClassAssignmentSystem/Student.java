package ClassAssignmentSystem;
import javafx.scene.control.CheckBox;

public class Student {

    private final String name;
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

    public boolean isSelected() {
        return selectBox.isSelected();
    }
}
