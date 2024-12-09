package ClassAssignmentSystem;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class Schedule {
    DayOfWeek day;
    LocalTime startTime;
    LocalTime endTime;

    Schedule(DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean overlapsWith(Schedule other) {
        if (this.day != other.day) {
            return false;
        }
        return this.startTime.isBefore(other.endTime) && other.startTime.isBefore(this.endTime);
    }
}
