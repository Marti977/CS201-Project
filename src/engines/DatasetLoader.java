package engines;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import models.Doctor;
import models.Patient;

/**
 * Reads the necessary data from the project's CSV dataset files.
 *
 * Each file must contain a header followed by one record per line.
 * Fields must appear in the same order as the supplied dataset files.
 */
public class DatasetLoader {

    /**
     * Loads patient records from a CSV file.
     *
     * @param fileName path to the patient CSV file
     * @return a list of patients in file order
     * @throws IOException if the file cannot be read or contains invalid data
     */
    public static List<Patient> loadPatients(String fileName) throws IOException {
        List<Patient> patients = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Path.of(fileName), StandardCharsets.UTF_8)) {
            String header = reader.readLine();

            if (header == null) {
                throw new IOException("Patient file is empty: " + fileName);
            }

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] fields = line.split(",", -1);
                if (fields.length != 6) {
                    throw new IOException("Expected 6 patient fields at line " + lineNumber + " in " + fileName);
                }

                try {
                    String name = fields[0].trim();
                    int age = Integer.parseInt(fields[1].trim());
                    String dob = fields[2].trim();
                    int conditionLevel = Integer.parseInt(fields[3].trim());
                    int criticalTimeLimit = Integer.parseInt(fields[4].trim());
                    int arrivalTick = Integer.parseInt(fields[5].trim());

                    if (name.isEmpty() || dob.isEmpty() || age < 0
                            || conditionLevel < 0 || conditionLevel > 10
                            || criticalTimeLimit < 0 || arrivalTick < 0) {
                        throw new IllegalArgumentException("Patient fields are outside the allowed range");
                    }

                    patients.add(new Patient(name, age, dob, conditionLevel,criticalTimeLimit, arrivalTick));
                } catch (IllegalArgumentException e) {
                    throw new IOException("Invalid patient data at line " + lineNumber + " in " + fileName + ": " + e.getMessage(), e);
                }
            }
        }

        return patients;
    }

    /**
     * Loads doctor records from a CSV file.
     *
     * @param fileName path to the doctor CSV file
     * @return a list of doctors in file order
     * @throws IOException if the file cannot be read or contains invalid data
     */
    public static List<Doctor> loadDoctors(String fileName) throws IOException {
        List<Doctor> doctors = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Path.of(fileName), StandardCharsets.UTF_8)) {
            String header = reader.readLine();

            if (header == null) {
                throw new IOException("Doctor file is empty: " + fileName);
            }

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] fields = line.split(",", -1);
                
                if (fields.length != 6) {
                    throw new IOException("Expected 6 doctor fields at line " + lineNumber + " in " + fileName);
                }

                try {
                    String name = fields[0].trim();
                    int age = Integer.parseInt(fields[1].trim());
                    String dob = fields[2].trim();
                    int skillLevel = Integer.parseInt(fields[3].trim());
                    int shiftStart = Integer.parseInt(fields[4].trim());
                    int shiftEnd = Integer.parseInt(fields[5].trim());

                    if (name.isEmpty() || dob.isEmpty() || age < 0
                            || skillLevel < 1 || shiftStart < 0
                            || shiftEnd <= shiftStart) {
                        throw new IllegalArgumentException("Doctor fields are outside the allowed range");
                    }

                    doctors.add(new Doctor(name, age, dob, skillLevel, shiftStart, shiftEnd
                    ));
                } catch (IllegalArgumentException e) {
                    throw new IOException("Invalid doctor data at line " + lineNumber + " in " + fileName + ": " + e.getMessage(), e);
                }
            }
        }

        return doctors;
    }
}