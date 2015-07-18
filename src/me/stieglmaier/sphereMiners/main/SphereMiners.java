package me.stieglmaier.sphereMiners.main;

import javafx.application.Application;
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

import java.io.IOException;
import java.net.MalformedURLException;

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
    public static void main(final String[] args) throws InvalidConfigurationException, IOException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parameters params = getParameters();

        Configuration config;
        if (params.getUnnamed().size() > 0) {
            config = Configuration.builder().loadFromFile(params.getUnnamed().get(0)).build();
        } else {
            config = Configuration.defaultConfiguration();
        }

        Model model = null;
        try {
            model = new Model(new PhysicsManager(config), new AIManager(config));
        } catch (ClassNotFoundException | MalformedURLException | InvalidConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println(getClass().getResource("sample.fxml"));
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Sphere Miners");
        primaryStage.setScene(new Scene(new GUI(model)));
        primaryStage.show();
    }

}
