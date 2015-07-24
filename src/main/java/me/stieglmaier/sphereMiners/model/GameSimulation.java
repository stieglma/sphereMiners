package me.stieglmaier.sphereMiners.model;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * This representation of a game can be displayed by the GUI.
 */
public final class GameSimulation {

    /**
     * A list containing all ticks of the game.
     */
    private final ObservableList<Tick> ticks = FXCollections.observableArrayList();
    private final List<ListChangeListener<Tick>> registeredListeners = new ArrayList<>();

    /**
     * Creates a new empty {@link GameSimulation}.
     */
    public GameSimulation() { /* nothing to do here*/}

    /**
     * Creates a new {@link GameSimulation} from a list of {@link Tick}s.
     *
     * @param simulationList List of created ticks.
     */
    public GameSimulation(List<Tick> simulationList) {
        ticks.addAll(simulationList);
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

    public void addObserver(ListChangeListener<Tick> listener) {
        ticks.addListener(listener);
        registeredListeners.add(listener);
    }

    public void removeObservers() {
        for (ListChangeListener<Tick> listener : registeredListeners) {
            ticks.removeListener(listener);
        }
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

    public int getSize() {
        return ticks.size();
    }
}
