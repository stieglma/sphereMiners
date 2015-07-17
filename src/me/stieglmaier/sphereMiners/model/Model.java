package me.stieglmaier.sphereMiners.model;

import java.util.List;
import java.util.Observable;
import java.util.ResourceBundle;

import com.google.common.base.Preconditions;

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
    private GameSimulation simulationView;

    /**
     * The thread where the games will be simulated.
     */
    private Thread simulation;

    /**
     * Indicates whether actually a simulation is running.
     */
    private volatile boolean existsSimulation = false;
    private volatile boolean isSimulationPaused = false;

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

        this.physMgr = Preconditions.checkNotNull(phys);
        this.aiMgr = Preconditions.checkNotNull(ai);

        aiMgr.setPhysicsManager(physMgr);
    }

    /**
     * {@inheritDoc}
     */
    public void simulateGame(final String ... ais) {
        if (!existsSimulation) {
            existsSimulation = true;
            simulation = new Thread(new Runnable() {

                @Override
                public void run() {

                    // reset old simulation
                    simulationView = new GameSimulation();
                    simulationView.addInstance(physMgr.createInitialTick(ais));

                    // now try to initialize a game with the given AIs.
                    // this needs the physMgr with a new simulation do
                    // not change the order
                    try {
                        aiMgr.initializeGameAIs(ais);

                        // a given AI could not be initialized or found at the
                        // given location
                    } catch (InstantiationException | InvalidAILocationException e) {
                        existsSimulation = false;
                        ResourceBundle bundle = ResourceBundle.getBundle("sphere_miners_language");
                        setChanged();

                        if (e instanceof InstantiationException) {
                            notifyObservers(bundle.getString("KI_INVALID"));
                        } else {
                            notifyObservers(bundle.getString("KI_LOCATION_INVALID"));
                        }

                        // abort execution because there is an error.
                        return;
                    }

                    while (true) {
                        // check runtime relevant flags
                        if (!existsSimulation) {
                            return;
                        } else {
                            while (isSimulationPaused) {
                                try {
                                    wait();
                                } catch (InterruptedException e) {
                                    // if they don't care we don't care...
                                    // one does not simply interrupt...
                                    // TODO is this correct?
                                }
                            }
                        }

                        // let the AIs apply their moves.
                        try {
                            aiMgr.applyMoves();
                        } catch (InterruptedException e) {
                            // probably a programming error, when this occurs
                            throw new RuntimeException(e);
                        }

                        // calculates the tick based on the AI moves.
                        // adds the finished tick.
                        simulationView.addInstance(physMgr.applyPhysics());
                    }
                }
            });
            simulation.setName("[jSoccer][CommunicationLayer]");
            simulation.start();
        }
    }

    /**
     * {@inheritDoc}
     */
    public GameSimulation getViewableSimulation() {
        return simulationView;
    }


    public void stopSimulation() {
        if (existsSimulation) {
            existsSimulation = false;
            isSimulationPaused = false;
        }
    }

    public void pauseSimulation() {
        if (existsSimulation) {
            isSimulationPaused = true;
        }
    }

    public void resumeSimulation() {
        if (existsSimulation) {
            isSimulationPaused = false;
            simulation.notify();
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAIList() {
        return aiMgr.getAIList();
    }

}
