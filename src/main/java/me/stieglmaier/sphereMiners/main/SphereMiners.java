package me.stieglmaier.sphereMiners.main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import me.stieglmaier.sphereMiners.model.Model;
import me.stieglmaier.sphereMiners.model.ai.AIManager;
import me.stieglmaier.sphereMiners.model.physics.PhysicsManager;
import me.stieglmaier.sphereMiners.view.GUI;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.configuration.OptionCollector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

/**
 * Main class of the program. Creates the model and the View in a new Thread and
 * adds the View as Observer to the model.
 */
public class SphereMiners extends Application {

    private static Model model;

    /**
     * Launches the application.
     *
     * @param args
     *            command line parameters are unused
     * @throws IOException
     * @throws InvalidConfigurationException
     */
    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Map<String, String>  params = getParameters().getNamed();
        Configuration config;

        // cmdline parameter to retrieve configuration options
        if (params.containsKey("printOptionsTo")) {
            try {
                new FileWriter(new File(params.get("printOptionsTo")), false).append(OptionCollector.getCollectedOptions(false)).close();
            } catch (IOException e) {
                System.err.println("Configuration Options file could not be written please recheck the given path.");
            }
            Platform.exit();
            return;

        } else if (params.containsKey("config")) {
            try {
                config = Configuration.builder().loadFromFile(params.get("config")).build();
            } catch (InvalidConfigurationException | IOException e) {
                System.err.println("Given configuration could not be parsed, now falling back to standard configuration." +
                                   "\n See the stacktrace for more information:");
                e.printStackTrace(System.err);
                config = Configuration.defaultConfiguration();
            }
        } else {
            config = Configuration.defaultConfiguration();
        }

        Model model = null;
        try {
            model = new Model(new PhysicsManager(config), new AIManager(config));
        } catch (ClassNotFoundException | MalformedURLException | InvalidConfigurationException e) {
            System.err.println("Model could not be created, shutting down.");
            e.printStackTrace(System.err);
        }

        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Sphere Miners");
        primaryStage.setScene(new Scene(new GUI(model)));
        primaryStage.show();
    }

}
