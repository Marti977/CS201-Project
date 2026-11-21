package ui;

import engines.SimulationEngine;
import java.io.IOException;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Doctor;
import models.Patient;

/**
 * JavaFX graphical interface for the Nota Hospital Simulation.
 * Displays real-time triage queues, active treatments, on-shift doctors,
 * tick event logs, and O(log_t n) B-Tree record lookups.
 */
public class Main extends Application {
    private final SimulationEngine engine = new SimulationEngine();

    private final Label tickLabel = new Label("Current Tick: 0");
    private final ListView<String> waitingList = new ListView<>();
    private final ListView<String> activeList = new ListView<>();
    private final ListView<String> doctorList = new ListView<>();
    private final ListView<String> eventAndDischargedList = new ListView<>();
    private final Label searchResultLabel = new Label("Enter Name, Age, and DOB to query the B-Tree.");

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(14));
        root.setStyle("-fx-background-color: #f4f6f9;");

        root.setTop(buildTopBar());
        root.setCenter(buildDashboardGrid());
        root.setBottom(buildLookupBar());

        refreshUI();

        Scene scene = new Scene(root, 1080, 720);
        primaryStage.setTitle("Nota Hospital — Triage & Resource Simulation");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox buildTopBar() {
        Label title = new Label("Nota Hospital Simulation");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        tickLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 6 12; "
                + "-fx-background-color: #e2e8f0; -fx-background-radius: 6;");

        Button stepButton = new Button("Next Tick (+1)");
        stepButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-padding: 8 16; -fx-cursor: hand;");
        stepButton.setOnAction(e -> {
            engine.stepTime();
            refreshUI();
        });

        Button resetButton = new Button("Reload CSV Datasets");
        resetButton.setStyle("-fx-padding: 8 12; -fx-cursor: hand;");
        resetButton.setOnAction(e -> {
            try {
                engine.loadDatasets("data/patients.csv", "data/doctors.csv");
                searchResultLabel.setText("Reloaded datasets from data/patients.csv and data/doctors.csv.");
            } catch (IOException ex) {
                searchResultLabel.setText("CSV reload error: " + ex.getMessage());
            }
            refreshUI();
        });

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(12, title, spacer, tickLabel, stepButton, resetButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 12, 0));
        return topBar;
    }

    private GridPane buildDashboardGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);

        VBox waitingBox = wrapPanel("Waiting Queue (Priority Triage Order)", waitingList);
        VBox activeBox = wrapPanel("Active Treatments (Being Seen)", activeList);
        VBox doctorBox = wrapPanel("On-Shift Doctors", doctorList);
        VBox logBox = wrapPanel("Tick Event Log & Discharged Patients", eventAndDischargedList);

        GridPane.setHgrow(waitingBox, Priority.ALWAYS);
        GridPane.setVgrow(waitingBox, Priority.ALWAYS);
        GridPane.setHgrow(activeBox, Priority.ALWAYS);
        GridPane.setVgrow(activeBox, Priority.ALWAYS);
        GridPane.setHgrow(doctorBox, Priority.ALWAYS);
        GridPane.setVgrow(doctorBox, Priority.ALWAYS);
        GridPane.setHgrow(logBox, Priority.ALWAYS);
        GridPane.setVgrow(logBox, Priority.ALWAYS);

        grid.add(waitingBox, 0, 0);
        grid.add(activeBox, 1, 0);
        grid.add(doctorBox, 0, 1);
        grid.add(logBox, 1, 1);
        return grid;
    }

    private VBox wrapPanel(String heading, ListView<String> listView) {
        Label label = new Label(heading);
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #334155;");
        VBox.setVgrow(listView, Priority.ALWAYS);

        VBox box = new VBox(6, label, listView);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; "
                + "-fx-border-radius: 6; -fx-background-radius: 6;");
        return box;
    }

    private VBox buildLookupBar() {
        Label lookupHeading = new Label("B-Tree Record Lookup (Composite Key: Name + Age + DOB)");
        lookupHeading.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #334155;");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Patient", "Doctor");
        typeBox.setValue("Patient");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name (e.g., John Doe)");
        nameField.setPrefWidth(200);

        TextField ageField = new TextField();
        ageField.setPromptText("Age (e.g., 30)");
        ageField.setPrefWidth(100);

        TextField dobField = new TextField();
        dobField.setPromptText("DOB (YYYY-MM-DD)");
        dobField.setPrefWidth(150);

        Button searchButton = new Button("Search B-Tree");
        searchButton.setStyle("-fx-background-color: #0f766e; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-padding: 6 14; -fx-cursor: hand;");

        searchButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String dob = dobField.getText().trim();
            if (name.isEmpty() || dob.isEmpty() || ageField.getText().trim().isEmpty()) {
                searchResultLabel.setText("Please fill in Name, Age, and DOB.");
                return;
            }
            try {
                int age = Integer.parseInt(ageField.getText().trim());
                if ("Patient".equals(typeBox.getValue())) {
                    Patient found = engine.lookupPatient(name, age, dob);
                    searchResultLabel.setText(found != null
                            ? "FOUND PATIENT -> " + found
                            : "No patient found matching (" + name + ", " + age + ", " + dob + ").");
                } else {
                    Doctor found = engine.lookupDoctor(name, age, dob);
                    searchResultLabel.setText(found != null
                            ? "FOUND DOCTOR -> " + found
                            : "No doctor found matching (" + name + ", " + age + ", " + dob + ").");
                }
            } catch (NumberFormatException ex) {
                searchResultLabel.setText("Invalid age: please enter a whole number.");
            }
        });

        searchResultLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a;");

        HBox controls = new HBox(10, typeBox, nameField, ageField, dobField, searchButton);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox bottomBox = new VBox(8, new Separator(), lookupHeading, controls, searchResultLabel);
        bottomBox.setPadding(new Insets(12, 0, 0, 0));
        return bottomBox;
    }

    private void refreshUI() {
        tickLabel.setText("Current Tick: " + engine.getCurrentTick());

        waitingList.getItems().clear();
        for (Patient p : engine.getWaitingPatients()) {
            waitingList.getItems().add(p.toString());
        }

        activeList.getItems().clear();
        for (Patient p : engine.getActivePatients()) {
            activeList.getItems().add(p.toString());
        }

        doctorList.getItems().clear();
        for (Doctor d : engine.getActiveDoctors()) {
            doctorList.getItems().add(d.toString());
        }

        eventAndDischargedList.getItems().clear();
        eventAndDischargedList.getItems().addAll(engine.getLastTickEvents());
        if (!engine.getDischargedPatients().isEmpty()) {
            eventAndDischargedList.getItems().add("--- Discharged Roster (" + engine.getDischargedPatients().size() + ") ---");
            for (Patient p : engine.getDischargedPatients()) {
                eventAndDischargedList.getItems().add(p.toString());
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}