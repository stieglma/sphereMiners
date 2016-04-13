package me.stieglmaier.sphereMiners.model;

import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import me.stieglmaier.sphereMiners.main.Constants;
import me.stieglmaier.sphereMiners.model.ai.AIManager;
import me.stieglmaier.sphereMiners.model.ai.AIManager.LoadingStatus;
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
  private final Consumer<List<Player>> badAICallback;

  /**
   * Creates a new {@link Model}.
   *
   * @param phys    The {@link Physics} to use for the simulation.
   * @param ai      The {@link AIManager} to use for the simulation.
   * @param constants The {@link Constants} to use for the simulation.
   * @param badAICallback The callBack to remove bad AIs from the list of playing AIs
   */
  public Model(
      final Physics phys,
      final AIManager ai,
      final Constants constants,
      final Consumer<List<Player>> badAICallback) {
    this.physics = phys;
    this.ais = ai;
    this.constants = constants;
    this.badAICallback = badAICallback;

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
    // create new Simulation
    simulationView = new GameSimulation();
    simulationView.addInstance(physics.createInitialTick(aisToPlay));

    simulation = new Simulation(ais, physics, aisToPlay, constants, badAICallback);
    simulation.start();

    return simulationView;
  }

  /**
   * Completely deletes the current simulation.
   */
  public void deleteSimulation() {
    if (simulationView != null) {
      simulationView = null;
      synchronized (simulation) {
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
    private final Consumer<List<Player>> badAICallback;

    public Simulation(
        AIManager ais,
        Physics physics,
        List<Player> aisToPlay,
        Constants constants,
        Consumer<List<Player>> badAICallback) {
      this.ais = ais;
      this.physMgr = physics;
      this.aisToPlay = aisToPlay;
      this.constants = constants;
      this.badAICallback = badAICallback;
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

      Map<Player, LoadingStatus> loadingStatus = ais.initializeGameAIs(aisToPlay);
      if (loadingStatus.values().contains(LoadingStatus.INITIALIZING_FAILED)) {
        Platform.runLater(
            ()
                -> ErrorPopup.create(
                    "Error while initializing AIs",
                    "Errors exist, either in the constructor or the init() method of the AIs:\n"
                        + loadingStatus
                            .entrySet()
                            .stream()
                            .filter(e -> e.getValue() == LoadingStatus.INITIALIZING_FAILED)
                            .reduce(
                                "",
                                (a, b) -> a + "\n" + b.getKey().getNameProperty().get(),
                                (a, b) -> a + "\n" + b),
                    null));
      } else if (loadingStatus.values().contains(LoadingStatus.INVALID_LOCATION)) {
        Platform.runLater(
            ()
                -> ErrorPopup.create(
                    "Error while loading AIs",
                    "The AI is not located at the given location, please refresh the List"
                        + "of usable AIs:\n"
                        + loadingStatus
                            .entrySet()
                            .stream()
                            .filter(e -> e.getValue() == LoadingStatus.INVALID_LOCATION)
                            .reduce(
                                "",
                                (a, b) -> a + "\n" + b.getKey().getNameProperty().get(),
                                (a, b) -> a + "\n" + b),
                    null));
      }

      badAICallback.accept(
          loadingStatus
              .entrySet()
              .stream()
              .filter(e -> e.getValue() != LoadingStatus.LOADED)
              .map(e -> e.getKey())
              .collect(Collectors.toList()));

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
        boolean isEnded = constants.getWinningCondition().hasGameEnded(simulationView, constants);
        if (isEnded) {
          nextTick = nextTick.toWinningTick(constants.getWinningCondition().getWinner());
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
