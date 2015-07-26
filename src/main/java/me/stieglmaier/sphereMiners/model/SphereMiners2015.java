package me.stieglmaier.sphereMiners.model;

import java.util.List;
import java.util.Map;
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
    /** All dots on the playground*/
    protected Set<Sphere> dots;
    /** The constants used for this game */
    protected Constants constants;

    private Physics physics;
    private Player ownAI;
    private Map<Player, List<MutableSphere>> allSpheres;
    private Map<Sphere, MutableSphere> sphereMap;
    private Turn currentTurn;
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

    /**
     * Sets the color of your spheres.
     *
     * @param color The color your spheres should have
     */
    protected final void setColor(Color color) {
        ownAI.setColor(color);
    }

    /**
     * Sets your displayed name.
     *
     * @param name The name you want to have
     */
    protected final void setName(String name) {
        ownAI.setName(name);
    }

    /**
     * Returns the Constants that are used throughout the framework.
     *
     * @return the Constants object used in the whole framework
     */
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
     *
     * @param spheres The map of spheres to their new (relative) moving directions
     *                (does not need to include all spheres you own)
     */
    protected final void changeMoveDirection(final Map<Sphere, Position> spheres) {
        currentTurn = () -> spheres.forEach((sphere, dir) -> sphereMap.get(sphere).setDirection(dir.normalize()));
    }

    /**
     * Splits the given sphere to two equally (half) sized ones.
     *
     * Executing this method prevents you from executing {@link SphereMiners2015#changeMoveDirection(Map)}
     * and {@link SphereMiners2015#merge(Sphere, Sphere)} in this turn. The last
     * called method of these will be executed.
     *
     * @param sphere The sphere you want to split into two parts
     */
    protected final void split(Sphere sphere) {
        // lists cannot be changed directly therefore we need the phyiscsmanager here
        currentTurn = () -> physics.split(sphereMap.get(sphere), ownAI);
    }

    /**
     * Merges the given spheres to one that has the accumulated size of both.
     *
     * Executing this method prevents you from executing {@link SphereMiners2015#changeMoveDirection(Map)}
     * and {@link SphereMiners2015#split(Sphere)} in this turn. The last
     * called method of these will be executed.
     *
     * @param sphere1 The sphere that should grow
     * @param sphere2 The sphere that should be merged into the other one
     */
    protected final void merge(Sphere sphere1, Sphere sphere2) {
        // lists cannot be changed directly therefore we need the phyiscsmanager here
        currentTurn = () -> physics.merge(sphereMap.get(sphere1),
                                          sphereMap.get(sphere2),
                                          ownAI);
    }

    /**
     * Returns the enemies surrounding the given sphere in a certain distance.
     *
     * @param sphere The sphere you want to find the surrounding enemies for
     * @return the sourrounding enemies of the given sphere
     */
    protected final Set<Sphere> getSurroundingEnemies(Sphere sphere) {
         return allSpheres.entrySet()
                  .stream()
                  .filter(e -> e.getKey() != ownAI)
                  .flatMap(l -> l.getValue().stream())
                  .filter(s -> s.getPosition().dist(sphere.getPosition()) <= constants.getSightDistance())
                  .map(s -> s.toImmutableSphere())
                  .collect(Collectors.toSet());
    }

    /**
     * Package private, this should only be called by AIManager!
     * @return indicates wether the turn could be evaluated within the timelimit
     *         or not
     */
    boolean evaluateTurn() {
        currentTurn = () -> {};
        dots = physics.getDots();
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

    /**
     * Sets the constants object for this ai.
     *
     * @param constants The constants that should be used for this ai.
     */
    void setConstants(Constants constants) {
        this.constants = constants;
    }

    /**
     * Package private, this should only be called and set by AImanager!
     *
     * @param physics the physics instance needed for some ai interactions
     */
    void setPhysics(Physics physics) {
        this.physics = physics;
        allSpheres = physics.getAISpheres();
        sphereMap = allSpheres.get(ownAI).stream()
                              .collect(Collectors.toMap(s -> s.toImmutableSphere(), s -> s));
        ownSpheres = sphereMap.keySet();
    }

    /**
     * Set the player used as identifier in maps throughout the framework
     *
     * @param player The internal representation of the player in the framework
     */
    void setPlayer(Player player) {
        this.ownAI = player;
    }

    @FunctionalInterface
    private interface Turn {
        void apply();
    }
}
