public class Doctor implements Comparable<Doctor> {
    public String name;
    public int age;
    public String dob;
    public int skillLevel;              // Higher skill = fewer ticks needed per patient
    public int shiftStart;
    public int shiftEnd;
    public boolean isBusy;
    public int remainingTreatmentTicks;
    public String currentPatientKey;

    public Doctor(String name, int age, String dob, int skillLevel, int shiftStart, int shiftEnd) {
        this.name = name;
        this.age = age;
        this.dob = dob;
        this.skillLevel = skillLevel;
        this.shiftStart = shiftStart;
        this.shiftEnd = shiftEnd;
        this.isBusy = false;
        this.remainingTreatmentTicks = 0;
        this.currentPatientKey = "";
    }

    // Lookup key constructor for B-Tree searches
    public Doctor(String name, int age, String dob) {
        this(name, age, dob, 1, 0, 24);
    }

    public String getKey() {
        return name + "|" + age + "|" + dob;
    }

    @Override
    public int compareTo(Doctor other) {
        return this.getKey().compareTo(other.getKey());
    }

    @Override
    public String toString() {
        return name + " (Skill " + skillLevel + ") | Shift: " + shiftStart + "-" + shiftEnd + (isBusy ? " [Busy]" : " [Available]");
    }
}