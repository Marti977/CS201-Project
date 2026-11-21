import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {
    private final SimulationEngine engine = new SimulationEngine();

    @Override
    public void start(Stage stage) {
        Label tickLabel = new Label("Current Tick: 0");
        Button nextTickBtn = new Button("Advance Time (+1 Tick)");

        nextTickBtn.setOnAction(e -> {
            engine.stepTime();
            tickLabel.setText("Current Tick: " + engine.getCurrentTick());
        });

        VBox root = new VBox(15, tickLabel, nextTickBtn);
        root.setPadding(new Insets(20));
        // TODO (Person 3): Add waiting/active patient lists and B-Tree lookup bar

        stage.setTitle("Nota Hospital Simulation");
        stage.setScene(new Scene(root, 900, 600));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}