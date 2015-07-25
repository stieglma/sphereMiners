package me.stieglmaier.sphereMiners.model;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javafx.scene.paint.Color;

import me.stieglmaier.sphereMiners.main.Constants;


public abstract class SphereMiners2015 {
    /** All owned spheres */
    protected Set<Sphere> ownSpheres;

    private Physics physMgr;
    private Player ownAI;
    private Map<Player, List<MutableSphere>> allSpheres;
    private Map<Sphere, MutableSphere> sphereMap;
    private Turn currentTurn;
    private Constants constants;
    private ExecutorService threadExecutor = Executors.newSingleThreadExecutor();

    /**
     * Set up your AI, initial values for attributes, color of your spheres, ...
     */
    protected abstract void init();

    /**
     * In this method your AI has to specify what it wants to do, it can either
     * change the moving direction of any number of spheres controlled by you, or
     * split a sphere into two (half) parts, or merge two spheres to a bigger one.
     *
     * Note: the or is exclusive in this case, only one of the above operations
     *       is allowed at the same time
     */
    protected abstract void playTurn();

    protected final void setColor(Color color) {
        ownAI.setColor(color);
    }

    protected final void setName(String name) {
        ownAI.setName(name);
    }

    protected final Constants getConstants() {
        return constants;
    }

    /**
     * Changes the moving direction of a Sphere instantly, there is no kind of
     * friction or acceleration in between. However the speed cannot be changed
     * with this method, as it is dependant on the size of the Sphere and cannot
     * be set by a player.
     *
     * Executing this method prevents you from executing {@link SphereMiners2015#split(Sphere)}
     * and {@link SphereMiners2015#merge(Sphere, Sphere)} in this turn. The last
     * called method of these will be executed.
     */
    protected final void changeMoveDirection(final Map<Sphere, Position> spheres) {
        currentTurn = () -> spheres.forEach((sphere, dir) -> sphereMap.get(sphere)
                                                                      .setDirection(dir));
    }

    /**
     * Splits the given sphere to two equally (half) sized ones.
     *
     * Executing this method prevents you from executing {@link SphereMiners2015#changeMoveDirection(Map)}
     * and {@link SphereMiners2015#merge(Sphere, Sphere)} in this turn. The last
     * called method of these will be executed.
     */
    protected final void split(Sphere sphere) {
        // lists cannot be changed directly therefore we need the phyiscsmanager here
        currentTurn = () -> physMgr.split(sphereMap.get(sphere), ownAI);
    }

    /**
     * Merges the given spheres to one that has the accumulated size of both.
     *
     * Executing this method prevents you from executing {@link SphereMiners2015#changeMoveDirection(Map)}
     * and {@link SphereMiners2015#split(Sphere)} in this turn. The last
     * called method of these will be executed.
     */
    protected final void merge(Sphere sphere1, Sphere sphere2) {
        // lists cannot be changed directly therefore we need the phyiscsmanager here
        currentTurn = () -> physMgr.merge(sphereMap.get(sphere1),
                                          sphereMap.get(sphere2),
                                          ownAI);
    }

    /**
     * Returns the enemies surrounding the given sphere in a certain distance.
     * @param sphere
     * @return
     */
    protected final Set<MutableSphere> getSurroundingEnemies(Sphere sphere) {
        // TODO implement this operation
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Package private, this should only be called by AIManager!
     * @return indicates wether the turn could be evaluated within the timelimit
     *         or not
     */
    boolean evaluateTurn() {
        currentTurn = () -> {};
        Future<?> future = threadExecutor.submit(() -> playTurn());
        try {
            future.get(constants.getAIComputationTime(), TimeUnit.MILLISECONDS);
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            future.cancel(true);
            // TODO proved exception information in logger (introduce logging first)
            return false;
        }
        currentTurn.apply();
        return true;
    }

    void setConstants(Constants constants) {
        this.constants = constants;
    }

    /**
     * Package private, this should only be called and set by AImanager!
     */
    void setPhysics(Physics mgr) {
        physMgr = mgr;
        allSpheres = mgr.getAISpheres();
        sphereMap = allSpheres.get(ownAI).stream()
                              .collect(Collectors.toMap(s -> s.toImmutableSphere(), s -> s));
        ownSpheres = sphereMap.keySet();
    }

    /**
     * Set the name used as identifier in maps throughout the framework
     */
    void setPlayer(Player name) {
        this.ownAI = name;
    }

    @FunctionalInterface
    private interface Turn {
        void apply();
    }
}
