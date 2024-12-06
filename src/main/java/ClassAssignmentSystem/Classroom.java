package ClassAssignmentSystem;

public class Classroom {
    private String classroomID;
    private int capacity;

    public Classroom(String classroomID, int capacity) {
        this.classroomID = classroomID;
        this.capacity = capacity;
    }

    // Getters
    public String getClassroomID() {
        return classroomID;
    }

    public int getCapacity() {
        return capacity;
    }

    // Optional: Setters if you plan to modify classroom details in the future
}
