package me.stieglmaier.sphereMiners.model;

import java.util.List;
import java.util.Observable;
import java.util.logging.Level;

import javafx.application.Platform;
import javafx.collections.ObservableList;

import me.stieglmaier.sphereMiners.exceptions.InvalidAILocationException;
import me.stieglmaier.sphereMiners.main.Constants;
import me.stieglmaier.sphereMiners.model.ai.AIManager;
import me.stieglmaier.sphereMiners.model.ai.Player;
import me.stieglmaier.sphereMiners.model.physics.Physics;
import me.stieglmaier.sphereMiners.model.util.GameSimulation;
import me.stieglmaier.sphereMiners.model.util.Tick;
import me.stieglmaier.sphereMiners.view.ErrorPopup;


/**
 * The model class connects the whole backend of the framework. It is responsible
 * for starting a simulation and setting up all necessary parts.
 *
 * @author stieglma
 *
 */
public class Model extends Observable {

    private final Physics physics;
    private final AIManager ais;
    private static GameSimulation simulationView;
    private Simulation simulation;
    private final Constants constants;

    /**
     * Creates a new {@link Model}.
     *
     * @param phys    The {@link Physics} to use for the simulation.
     * @param ai      The {@link AIManager} to use for the simulation.
     */
    public Model(final Physics phys, final AIManager ai, final Constants constants) {
        this.physics = phys;
        this.ais = ai;
        this.constants = constants;

        ais.setPhysics(physics);
    }

    /**
     * Simulates a game and returns the simulation object where the ticks
     * are saved into.
     *
     * @param aisToPlay the list of players that should play a game
     * @return the SimulationObject that can be viewed
     */
    public GameSimulation simulateGame(final List<Player> aisToPlay) {
        if (simulationView == null) {
            // create new Simulation
            simulationView = new GameSimulation();
            simulationView.addInstance(physics.createInitialTick(aisToPlay));

            simulation = new Simulation(ais, physics, aisToPlay, constants);
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
     *
     * @return the list of ais that can be used for playing
     */
    public ObservableList<String> getAIList() {
        return ais.getAIList();
    }

    private static class Simulation extends Thread {
        private boolean isRunning = false;
        private boolean stopSimulation = false;
        private final Physics physMgr;
        private final AIManager ais;
        private final List<Player> aisToPlay;
        private final Constants constants;

        public Simulation(AIManager ais, Physics physics, List<Player> aisToPlay, Constants constants) {
            this.ais = ais;
            this.physMgr = physics;
            this.aisToPlay = aisToPlay;
            this.constants = constants;
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

        @Override
        public void run() {
            isRunning = true;

            try {
                ais.initializeGameAIs(aisToPlay);

                // a given AI could not be initialized or found at the
                // given location
            } catch (InstantiationException | InvalidAILocationException e) {
                Platform.runLater(() -> ErrorPopup.create("Error while initializing AIs",
                                  "Please reload the list of AIs that can be used for playing", e));
                return;
            }

            // let the AIs apply their moves and
            // calculate the tick based on them
            // adds the finished tick to the simulation object
            while (!stopSimulation) {
                synchronized (this) {
                    while (!isRunning) {
                       try {
                          wait();
                       } catch (InterruptedException e) {
                          constants.getLogger().logException(Level.WARNING, e, "");
                       }
                    }
                 }

                ais.applyMoves();
                Tick nextTick = physMgr.applyPhysics();

                // is the game over?
                boolean isEnded = constants.getWinningRule().hasGameEnded(simulationView, constants);
                if (isEnded) {
                    nextTick = nextTick.toWinningTick(constants.getWinningRule().getWinner());
                }
                simulationView.addInstance(nextTick);
                // end this thread if game is finished
                if (isEnded) {
                    return;
                }
            }
        }
    }
}
