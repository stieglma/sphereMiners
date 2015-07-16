package me.stieglmaier.sphereMiners.main;

import java.awt.EventQueue;

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
                // new gui
            }
        });
    }

    /**
     * Private constructor to prevent wrong initialization.
     */
    private SphereMiners() {
    }

}
