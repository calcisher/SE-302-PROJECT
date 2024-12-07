package ClassAssignmentSystem;

public class Schedule {
    private String startTime;
    private String endTime;

    public Schedule(String startTime, String endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public boolean isConflict(Schedule otherSchedule) {
        return false;
    }
}
