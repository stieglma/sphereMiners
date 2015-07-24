package me.stieglmaier.sphereMiners.model;

import java.util.List;
import java.util.Observable;

import javafx.collections.ObservableList;
import me.stieglmaier.sphereMiners.exceptions.InvalidAILocationException;

public class Model extends Observable {

    /**
     * The {@link Physics}.
     */
    private final Physics physics;

    /**
     * The {@link AIs}.
     */
    private final AIs ais;

    /**
     * The {@link SimulationToViewManager}.
     */
    private static GameSimulation simulationView;

    /**
     * The thread where the games will be simulated.
     */
    private Simulation simulation;

    /**
     * Creates a new {@link CommunicationLayer}.
     *
     * @param phys    The {@link Physics} to use for the simulation.
     * @param ai      The {@link AIs} to use for the simulation.
     * @param simView The {@link SimulationToViewManager} to use for the simulation.
     * @throws IllegalArgumentException if a reference of the given managers is null an exception
     *                                  will be thrown to prevent an illegal state.
     */
    public Model(final Physics phys, final AIs ai) {
        this.physics = phys;
        this.ais = ai;

        ais.setPhysics(physics);
        physics.setAIManager(ais);
    }

    /**
     * Simulates a game and returns the simulation object where the ticks
     * are saved into.
     */
    public GameSimulation simulateGame(final List<Player> aisToPlay) {
        if (simulationView == null) {
            // create new Simulation
            simulationView = new GameSimulation();
            simulationView.addInstance(physics.createInitialTick(aisToPlay));

            simulation = new Simulation(ais, physics, aisToPlay);
            simulation.start();
        }

        return simulationView;
    }

    /**
     * Completely deletes the current simulation.
     */
    public void deleteSimulation() {
        if (simulationView != null) {
            simulationView = null;
            synchronized(simulation) {
                simulation.stopSimulation();
            }
        }
    }

    /**
     * Pauses or Resumes the current simulation.
     */
    public void pauseSimulation() {
        if (simulationView != null) {
            synchronized (simulation) {
                simulation.pauseResume();
            }
        }
    }

    /**
     * Returns the list of AIs that can be used for playing
     */
    public ObservableList<String> getAIList() {
        return ais.getAIList();
    }

    private static class Simulation extends Thread {
        private boolean isRunning = false;
        private boolean stopSimulation = false;
        private final Physics physMgr;
        private final AIs ais;
        private final List<Player> aisToPlay;

        public Simulation(AIs ais, Physics physics, List<Player> aisToPlay) {
            this.ais = ais;
            this.physMgr = physics;
            this.aisToPlay = aisToPlay;
            setName("[sphereMiners][simulationThread]");
        }

        public void pauseResume() {
            isRunning = !isRunning;
            if (isRunning) {
                notify();
            }
        }

        public void stopSimulation() {
            stopSimulation = true;
        }

        public void run() {
            isRunning = true;

         // now try to initialize a game with the given AIs.
            // this needs the physMgr with a new simulation do
            // not change the order
            try {
                ais.initializeGameAIs(aisToPlay);

                // a given AI could not be initialized or found at the
                // given location
            } catch (InstantiationException | InvalidAILocationException e) {
                // TODO populate this error to view, no crash necessary
                throw new RuntimeException("Error while loading AIs", e);
            }

            // let the AIs apply their moves and
            // calculate the tick based on them
            // adds the finished tick to the simulation object
            while (!stopSimulation) {
                synchronized (this) {
                    while (!isRunning) {
                       try {
                          wait();
                       } catch (Exception e) {
                          e.printStackTrace();
                          // TODO what to do here? Just ignore probably
                       }
                    }
                 }

                try {
                    simulationView.addInstance(physMgr.applyPhysics());
                } catch (IllegalArgumentException | InterruptedException e) {
                    // whis will most likely be a programming error,
                    // rethrow and hope it doesn't occur
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
