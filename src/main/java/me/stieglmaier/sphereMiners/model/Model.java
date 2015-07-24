package me.stieglmaier.sphereMiners.model;

import java.util.List;
import java.util.Observable;

import javafx.collections.ObservableList;
import me.stieglmaier.sphereMiners.exceptions.InvalidAILocationException;
import me.stieglmaier.sphereMiners.model.ai.AIManager;
import me.stieglmaier.sphereMiners.model.physics.PhysicsManager;

public class Model extends Observable {

    /**
     * The {@link PhysicsManager}.
     */
    private final PhysicsManager physMgr;

    /**
     * The {@link AIManager}.
     */
    private final AIManager aiMgr;

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
     * @param phys    The {@link PhysicsManager} to use for the simulation.
     * @param ai      The {@link AIManager} to use for the simulation.
     * @param simView The {@link SimulationToViewManager} to use for the simulation.
     * @throws IllegalArgumentException if a reference of the given managers is null an exception
     *                                  will be thrown to prevent an illegal state.
     */
    public Model(final PhysicsManager phys, final AIManager ai) {
        this.physMgr = phys;
        this.aiMgr = ai;

        aiMgr.setPhysicsManager(physMgr);
        physMgr.setAIManager(aiMgr);
    }

    /**
     * Simulates a game and returns the simulation object where the ticks
     * are saved into.
     */
    public GameSimulation simulateGame(final List<String> ais) {
        if (simulationView == null) {
            // create new Simulation
            simulationView = new GameSimulation();
            simulationView.addInstance(physMgr.createInitialTick(ais));

            simulation = new Simulation(aiMgr, physMgr, ais);
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
        return aiMgr.getAIList();
    }

    private static class Simulation extends Thread {
        private boolean isRunning = false;
        private boolean stopSimulation = false;
        private final PhysicsManager physMgr;
        private final AIManager aiMgr;
        private final List<String> ais;

        public Simulation(AIManager aiMgr, PhysicsManager physMgr, List<String> ais) {
            this.aiMgr = aiMgr;
            this.physMgr = physMgr;
            this.ais = ais;
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
                aiMgr.initializeGameAIs(ais);

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
