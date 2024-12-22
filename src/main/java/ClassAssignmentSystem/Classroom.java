package ClassAssignmentSystem;

public class Classroom {
    private final String name;
    private final int capacity;
    private boolean[] availability; //for time intervals

    public Classroom(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    // Optional: Setters if you plan to modify classroom details in the future
}
