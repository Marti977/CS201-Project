package models;

public class Patient implements Comparable<Patient> {
    public String name;
    public int age;
    public String dob;               // Unique combined with name + age
    public int conditionLevel;       // 0 = healthy, 10 = critical
    public int criticalTimeLimit;    // Ticks before escalating to critical (10)
    public int arrivalTick;
    public int timeWaited;
    public boolean isBeingSeen;
    public boolean isDischarged;

    public Patient(String name, int age, String dob, int conditionLevel, int criticalTimeLimit, int arrivalTick) {
        this.name = name;
        this.age = age;
        this.dob = dob;
        this.conditionLevel = conditionLevel;
        this.criticalTimeLimit = criticalTimeLimit;
        this.arrivalTick = arrivalTick;
        this.timeWaited = 0;
        this.isBeingSeen = false;
        this.isDischarged = false;
    }

    // Lookup key constructor for B-Tree searches
    public Patient(String name, int age, String dob) {
        this(name, age, dob, 0, 0, 0);
    }

    public String getKey() {
        return name + "|" + age + "|" + dob;
    }

    @Override
    public int compareTo(Patient other) {
        return this.getKey().compareTo(other.getKey());
    }

    @Override
    public String toString() {
        return name + " (Age " + age + ", DOB: " + dob + ") | Cond: " + conditionLevel + "/10 | Waited: " + timeWaited + "/" + criticalTimeLimit;
    }
}