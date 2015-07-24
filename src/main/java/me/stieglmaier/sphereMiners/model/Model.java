package me.stieglmaier.sphereMiners.model;

import java.util.List;
import java.util.Observable;
import java.util.ResourceBundle;

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
        this.physMgr = phys;
        this.aiMgr = ai;

        aiMgr.setPhysicsManager(physMgr);
        physMgr.setAIManager(aiMgr);
    }

    public static int[] toPrimitive(Integer[] IntegerArray) {
        
        int[] result = new int[IntegerArray.length];
        for (int i = 0; i < IntegerArray.length; i++) {
                result[i] = IntegerArray[i].intValue();
        }
        return result;
}

    /**
     * {@inheritDoc}
     */
    public GameSimulation simulateGame(final List<String> ais) {
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
                        try {
                            simulationView.addInstance(physMgr.applyPhysics());
                        } catch (IllegalArgumentException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            });
            simulation.setName("[jSoccer][CommunicationLayer]");
            simulation.start();
        }

        return simulationView;
    }

    public void deleteSimulation() {
        if (existsSimulation) {
            existsSimulation = false;
            isSimulationPaused = false;
        }
    }

    public void pauseSimulation() {
        if (existsSimulation) {
            isSimulationPaused = !isSimulationPaused;
            if (!isSimulationPaused) {
                simulation.notify();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public ObservableList<String> getAIList() {
        return aiMgr.getAIList();
    }

}
