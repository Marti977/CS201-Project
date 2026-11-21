package engines;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import models.Doctor;
import models.Patient;
import structures.BTree;

/**
 * Core discrete-time simulation engine for Nota Hospital.
 * Manages patient arrivals, PriorityQueue triage, critical-state escalation,
 * doctor shift schedules, and skill-based treatment durations.
 */
public class SimulationEngine {
    private int currentTick = 0;

    // B-Trees for O(log_t n) lookup of all registered patients and doctors
    private final BTree<Patient> patientTree = new BTree<>(3);
    private final BTree<Doctor> doctorTree = new BTree<>(3);

    // Master collections for tracking simulation state across ticks
    private final List<Patient> scheduledArrivals = new ArrayList<>();
    private final List<Patient> activePatients = new ArrayList<>();
    private final List<Patient> dischargedPatients = new ArrayList<>();
    private final List<Doctor> allDoctors = new ArrayList<>();
    private final List<String> tickEventLog = new ArrayList<>();

    // Triage PriorityQueue: highest conditionLevel first (10 = critical),
    // then closest to criticalTimeLimit, then earliest arrivalTick
    private final PriorityQueue<Patient> waitingQueue = new PriorityQueue<>(
        Comparator.comparingInt((Patient p) -> p.conditionLevel).reversed()
            .thenComparingInt(p -> (p.criticalTimeLimit - p.timeWaited))
            .thenComparingInt(p -> p.arrivalTick)
    );

    public SimulationEngine() {
        try {
            loadDatasets("data/patients.csv", "data/doctors.csv");
        } catch (IOException e) {
            System.out.println("[Info] CSV datasets not loaded (" + e.getMessage() + "). Using fallback seed data.");
            seedDefaultData();
        }
    }

    /**
     * Loads patient and doctor records from CSV files using DatasetLoader
     * and inserts them into the B-Trees and simulation schedules.
     */
    public void loadDatasets(String patientFile, String doctorFile) throws IOException {
        List<Patient> loadedPatients = DatasetLoader.loadPatients(patientFile);
        List<Doctor> loadedDoctors = DatasetLoader.loadDoctors(doctorFile);

        clearAllData();
        for (Patient p : loadedPatients) {
            registerPatient(p);
        }
        for (Doctor d : loadedDoctors) {
            registerDoctor(d);
        }
    }

    private void seedDefaultData() {
        clearAllData();
        registerPatient(new Patient("John Doe", 30, "1996-05-12", 7, 3, 0));
        registerPatient(new Patient("John Doe", 30, "1996-11-03", 4, 5, 0));
        registerPatient(new Patient("Jane Smith", 45, "1981-02-20", 9, 2, 1));
        registerPatient(new Patient("Alex Rivera", 22, "2004-07-19", 6, 4, 1));
        registerPatient(new Patient("Sam Patel", 61, "1965-01-30", 8, 2, 3));

        registerDoctor(new Doctor("Dr. House", 50, "1976-06-11", 5, 0, 8));
        registerDoctor(new Doctor("Dr. Cuddy", 44, "1982-09-15", 3, 0, 8));
        registerDoctor(new Doctor("Dr. Wilson", 46, "1980-04-02", 4, 6, 16));
    }

    public void clearAllData() {
        currentTick = 0;
        scheduledArrivals.clear();
        waitingQueue.clear();
        activePatients.clear();
        dischargedPatients.clear();
        allDoctors.clear();
        tickEventLog.clear();
    }

    public void registerPatient(Patient p) {
        patientTree.insert(p);
        if (p.arrivalTick <= currentTick && !p.isBeingSeen && !p.isDischarged) {
            waitingQueue.offer(p);
        } else if (!p.isBeingSeen && !p.isDischarged) {
            scheduledArrivals.add(p);
        }
    }

    public void registerDoctor(Doctor d) {
        doctorTree.insert(d);
        allDoctors.add(d);
    }

    /**
     * Advances the hospital simulation by exactly 1 discrete time tick.
     */
    public void stepTime() {
        currentTick++;
        tickEventLog.clear();
        tickEventLog.add("=== Tick " + currentTick + " ===");

        // 1. Progress active treatments and discharge finished patients
        processActiveTreatments();

        // 2. Admit all patients arriving at this tick (supports simultaneous arrivals)
        processArrivals();

        // 3. Increment wait times for queued patients and escalate to Critical (10) if overdue
        updateWaitingPatientsAndEscalate();

        // 4. Assign available on-shift doctors to the highest-priority waiting patients
        assignDoctorsToPatients();
    }

    private void processActiveTreatments() {
        for (Doctor doc : allDoctors) {
            if (doc.isBusy) {
                doc.remainingTreatmentTicks--;
                if (doc.remainingTreatmentTicks <= 0) {
                    Patient treated = findActivePatientByKey(doc.currentPatientKey);
                    if (treated != null) {
                        treated.isBeingSeen = false;
                        treated.isDischarged = true;
                        treated.conditionLevel = 0; // Healthy upon discharge
                        activePatients.remove(treated);
                        dischargedPatients.add(treated);
                        tickEventLog.add("DISCHARGED: " + treated.name + " treated by " + doc.name);
                    }
                    doc.isBusy = false;
                    doc.currentPatientKey = "";
                }
            }
        }
    }

    private void processArrivals() {
        List<Patient> arrivedNow = new ArrayList<>();
        for (Patient p : scheduledArrivals) {
            if (p.arrivalTick <= currentTick) {
                arrivedNow.add(p);
                waitingQueue.offer(p);
                tickEventLog.add("ARRIVED: " + p.name + " (Cond: " + p.conditionLevel + "/10)");
            }
        }
        scheduledArrivals.removeAll(arrivedNow);
    }

    private void updateWaitingPatientsAndEscalate() {
        // Drain and re-insert into PriorityQueue so heap ordering updates after priority changes
        List<Patient> stillWaiting = new ArrayList<>();
        while (!waitingQueue.isEmpty()) {
            Patient p = waitingQueue.poll();
            p.timeWaited++;

            if (p.timeWaited >= p.criticalTimeLimit && p.conditionLevel < 10) {
                p.conditionLevel = 10; // Escalate to critical state
                tickEventLog.add("CRITICAL ESCALATION: " + p.name + " reached condition 10!");
            }
            stillWaiting.add(p);
        }
        waitingQueue.addAll(stillWaiting);
    }

    private void assignDoctorsToPatients() {
        List<Doctor> availableDoctors = new ArrayList<>();
        for (Doctor doc : allDoctors) {
            if (!doc.isBusy && isDoctorOnShift(doc, currentTick)) {
                availableDoctors.add(doc);
            }
        }

        // Pair highest-skill available doctors with the most critical waiting patients first
        availableDoctors.sort(Comparator.comparingInt((Doctor d) -> d.skillLevel).reversed());

        for (Doctor doc : availableDoctors) {
            if (waitingQueue.isEmpty()) {
                break;
            }
            Patient nextPatient = waitingQueue.poll();
            int duration = calculateTreatmentTicks(nextPatient, doc);

            nextPatient.isBeingSeen = true;
            doc.isBusy = true;
            doc.remainingTreatmentTicks = duration;
            doc.currentPatientKey = nextPatient.getKey();
            activePatients.add(nextPatient);

            tickEventLog.add("ASSIGNED: " + doc.name + " (Skill " + doc.skillLevel + ") -> "
                    + nextPatient.name + " (Cond " + nextPatient.conditionLevel + ", " + duration + " ticks)");
        }
    }

    /**
     * Calculates treatment duration in ticks: sicker patients take longer,
     * while higher-skill doctors finish faster (minimum 1 tick).
     */
    private int calculateTreatmentTicks(Patient p, Doctor d) {
        int effectiveSkill = Math.max(1, d.skillLevel);
        return Math.max(1, (int) Math.ceil((double) p.conditionLevel / effectiveSkill));
    }

    /**
     * Checks whether a doctor is working at the given simulation tick (24-hour cycle).
     */
    public boolean isDoctorOnShift(Doctor doc, int tick) {
        int hourOfDay = tick % 24;
        if (doc.shiftStart <= doc.shiftEnd) {
            return hourOfDay >= doc.shiftStart && hourOfDay < doc.shiftEnd;
        } else {
            return hourOfDay >= doc.shiftStart || hourOfDay < doc.shiftEnd;
        }
    }

    private Patient findActivePatientByKey(String key) {
        for (Patient p : activePatients) {
            if (p.getKey().equals(key)) {
                return p;
            }
        }
        return null;
    }

    // --- Public Getters & B-Tree Lookup API for UI ---

    public int getCurrentTick() {
        return currentTick;
    }

    /** Returns waiting patients ordered by triage priority (highest condition first). */
    public List<Patient> getWaitingPatients() {
        List<Patient> ordered = new ArrayList<>(waitingQueue);
        ordered.sort(waitingQueue.comparator());
        return ordered;
    }

    public List<Patient> getActivePatients() {
        return new ArrayList<>(activePatients);
    }

    public List<Patient> getDischargedPatients() {
        return new ArrayList<>(dischargedPatients);
    }

    /** Returns only doctors currently on shift at currentTick. */
    public List<Doctor> getActiveDoctors() {
        List<Doctor> onShift = new ArrayList<>();
        for (Doctor d : allDoctors) {
            if (isDoctorOnShift(d, currentTick)) {
                onShift.add(d);
            }
        }
        return onShift;
    }

    public List<Doctor> getAllDoctors() {
        return new ArrayList<>(allDoctors);
    }

    public List<String> getLastTickEvents() {
        return new ArrayList<>(tickEventLog);
    }

    public Patient lookupPatient(String name, int age, String dob) {
        return patientTree.search(new Patient(name, age, dob));
    }

    public Doctor lookupDoctor(String name, int age, String dob) {
        return doctorTree.search(new Doctor(name, age, dob));
    }
}