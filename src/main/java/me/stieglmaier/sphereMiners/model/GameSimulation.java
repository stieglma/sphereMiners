package me.stieglmaier.sphereMiners.model;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This representation of a game can be displayed by the GUI.
 */
public final class GameSimulation {

    /**
     * A list containing all ticks of the game.
     */
    private List<Tick> ticks;

    /**
     * Creates a new empty {@link GameSimulation}.
     */
    public GameSimulation() {
        ticks = new ArrayList<>();
    }

    /**
     * Creates a new {@link GameSimulation} from a list of {@link Tick}s.
     *
     * @param simulationList List of created ticks.
     */
    public GameSimulation(List<Tick> simulationList) {
        ticks = requireNonNull(simulationList);
    }

    /**
     * Adds a new {@link Tick} to the SimulationObject.
     *
     * @param tick The {@link Tick} to add.
     * @throws IllegalArgumentException
     *             If the tick-parameter is invalid (e.g. null) this exception
     *             will be thrown to prevent an invalid state.
     */
    public void addInstance(final Tick tick) {
        ticks.add(requireNonNull(tick));
    }

    /**
     * Returns the tick at a specific position in the game.
     *
     * @param numberOfTick The number of the {@link Tick} to return.
     * @return The requested {@link Tick}. Null if the requested
     *         tick is not available yet or out of range (e.g. bigger than
     *         maximum number of ticks).
     */
    public Tick getTick(final int numberOfTick) {
        if (numberOfTick < 0 || numberOfTick > ticks.size()) {
            return null;
        } else {
            return ticks.get(numberOfTick);
        }
    }

}
