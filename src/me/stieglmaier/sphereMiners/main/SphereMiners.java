package me.stieglmaier.sphereMiners.main;

import java.awt.EventQueue;
import java.io.IOException;
import java.net.MalformedURLException;

import me.stieglmaier.sphereMiners.model.Model;
import me.stieglmaier.sphereMiners.model.ai.AIManager;
import me.stieglmaier.sphereMiners.model.physics.PhysicsManager;

import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;

/**
 * Main class of the program. Creates the model and the View in a new Thread and
 * adds the View as Observer to the model.
 */
public class SphereMiners {

    /**
     * Launches the application.
     *
     * @param args
     *            command line parameters are unused
     * @throws IOException 
     * @throws InvalidConfigurationException 
     */
    public static void main(final String[] args) throws InvalidConfigurationException, IOException {

        Configuration config;
        if (args.length > 0) {
            config = Configuration.builder().loadFromFile(args[0]).build();
        } else {
            config = Configuration.defaultConfiguration();
        }

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                Model model = null;
                try {
                    model = new Model(new PhysicsManager(config), new AIManager(config));
                } catch (ClassNotFoundException | MalformedURLException | InvalidConfigurationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                model.simulateGame("ais.DumbAI", "ais.DumbAI2", "ais.DumbAI3", "ais.DumbAI4", "ais.DumbAI5");
                // new gui with config
            }
        });
    }

    /**
     * Private constructor to prevent wrong initialization.
     */
    private SphereMiners() {
    }

}
