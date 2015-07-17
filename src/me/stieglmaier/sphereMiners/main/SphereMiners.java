package me.stieglmaier.sphereMiners.main;

import java.awt.EventQueue;
import java.io.IOException;

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
     */
    public static void main(final String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                Configuration config;
                try {
                    config = Configuration.builder().loadFromFile(args[0]).build();
                } catch (IOException | InvalidConfigurationException e) {
                    // TODO logging - fallback to default
                    config = Configuration.defaultConfiguration();
                }
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
