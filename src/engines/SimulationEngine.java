package engines;

import java.util.ArrayList;
import java.util.List;
import models.Doctor;
import models.Patient;
import structures.BTree;

import java.util.ArrayList;
import java.util.List;

public class SimulationEngine {
    private int currentTick = 0;
    private final BTree<Patient> patientTree = new BTree<>(3);
    private final BTree<Doctor> doctorTree = new BTree<>(3);

    // Temporary dummy lists so JavaFX renders immediately
    private final List<Patient> dummyWaiting = new ArrayList<>();
    private final List<Patient> dummyActive = new ArrayList<>();
    private final List<Doctor> dummyDoctors = new ArrayList<>();

    public SimulationEngine() {
        // Seed dummy data (includes duplicate name/age with distinct DOBs)
        Patient p1 = new Patient("John Doe", 30, "1996-05-12", 7, 5, 0);
        Patient p2 = new Patient("John Doe", 30, "1996-11-03", 4, 8, 0);
        Patient p3 = new Patient("Jane Smith", 45, "1981-02-20", 9, 2, 0);
        p3.isBeingSeen = true;

        Doctor d1 = new Doctor("Dr. House", 50, "1976-06-11", 5, 0, 12);
        d1.isBusy = true;
        d1.remainingTreatmentTicks = 2;
        d1.currentPatientKey = p3.getKey();

        Doctor d2 = new Doctor("Dr. Cuddy", 44, "1982-09-15", 4, 0, 12);

        patientTree.insert(p1);
        patientTree.insert(p2);
        patientTree.insert(p3);
        doctorTree.insert(d1);
        doctorTree.insert(d2);

        dummyWaiting.add(p1);
        dummyWaiting.add(p2);
        dummyActive.add(p3);
        dummyDoctors.add(d1);
        dummyDoctors.add(d2);
    }

    // Person 1: Parse dataset files and insert into patientTree / doctorTree
    public void loadDatasets(String patientFile, String doctorFile) {
        // TODO (Person 1)
    }

    // Person 2: Advance simulation by 1 tick
    public void stepTime() {
        currentTick++;
        // TODO (Person 2):
        // 1. Enqueue patients arriving at currentTick into a java.util.PriorityQueue
        // 2. Increment wait times; escalate conditionLevel to 10 if timeWaited >= criticalTimeLimit
        // 3. Complete treatments and enforce doctor shiftStart / shiftEnd
        // 4. Assign available on-shift doctors to highest-priority waiting patients
        System.out.println("[Stub] Advanced to tick " + currentTick);
    }

    // Person 3 (UI Getters & B-Tree Lookups)
    public int getCurrentTick() { return currentTick; }
    public List<Patient> getWaitingPatients() { return dummyWaiting; }
    public List<Patient> getActivePatients() { return dummyActive; }
    public List<Doctor> getActiveDoctors() { return dummyDoctors; }

    public Patient lookupPatient(String name, int age, String dob) {
        return patientTree.search(new Patient(name, age, dob));
    }

    public Doctor lookupDoctor(String name, int age, String dob) {
        return doctorTree.search(new Doctor(name, age, dob));
    }
}