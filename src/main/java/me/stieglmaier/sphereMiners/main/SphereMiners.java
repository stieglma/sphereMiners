package me.stieglmaier.sphereMiners.main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import me.stieglmaier.sphereMiners.controller.ViewController;
import me.stieglmaier.sphereMiners.model.Model;
import me.stieglmaier.sphereMiners.model.ai.AIManager;
import me.stieglmaier.sphereMiners.model.physics.PhysicsManager;

import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.configuration.OptionCollector;

import com.google.common.base.Optional;

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
        Optional<Configuration> config = handleOptions();
        if (!config.isPresent()) {
            // platform should be exited by handleOptions anyway
            return;
        }

        final Model model;
        final AIManager aiManager;
        try {
            aiManager = new AIManager(config.get());
            model = new Model(new PhysicsManager(config.get()), aiManager);
        } catch (ClassNotFoundException | MalformedURLException | InvalidConfigurationException e) {
            System.err.println("Model could not be created, shutting down.");
            e.printStackTrace(System.err);
            return;
        }

        primaryStage.setTitle("Sphere Miners");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Overview.fxml"));
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.setMinWidth(1280);
        primaryStage.setMinHeight(900);
        ViewController controller = (ViewController)loader.getController();

        controller.setAIList(model.getAIList());
        controller.setAIListListeners(() -> aiManager.reloadAIList());
        controller.setSimulateListeners(ais -> model.simulateGame(ais),
                                        ()  -> model.pauseSimulation(),
                                        ()  -> model.deleteSimulation());
        primaryStage.show();
    }

    private Optional<Configuration> handleOptions() {
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
            return Optional.absent();

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
        return Optional.of(config);
    }

}
