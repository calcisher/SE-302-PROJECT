package ClassAssignmentSystem;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Classroom classroom = (Classroom) o;
        return capacity == classroom.capacity &&
                Objects.equals(name, classroom.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, capacity);
    }

    // Optional: Setters if you plan to modify classroom details in the future
}
