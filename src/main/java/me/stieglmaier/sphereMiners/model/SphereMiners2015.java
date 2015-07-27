package me.stieglmaier.sphereMiners.model;

import java.util.Collection;
import java.util.Collections;
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
import java.util.stream.Stream;

import javafx.scene.paint.Color;
import me.stieglmaier.sphereMiners.main.Constants;


public abstract class SphereMiners2015 {
    /** All owned spheres */
    protected Set<Sphere> ownSpheres;
    /** All dots on the playground*/
    protected Set<Sphere> dots;

    private Physics physics;
    private Player ownAI;
    private Set<Sphere> allSpheres;
    private Turn currentMine;
    private Turn currentChangeDest;
    private Turn currentSplit;
    private Turn currentMerge;
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
     * Changes the moving direction of a (own) Sphere instantly, there is no kind
     * of friction or acceleration in between. However the speed cannot be changed
     * with this method, as it is dependant on the size of the Sphere and cannot
     * be set by a player.
     *
     * Execution order of merging, splitting and changing direction is at first
     * mine, then merge, then split and then change directions. All steps are
     * independant and need not to be set in every turn. Also splitting can not
     * be done on just merged spheres, as well as new spheres (by splitting)
     * cannot change direction in this turn.
     *
     * @param spheres The map of spheres to their new (relative) moving directions
     *                (does not need to include all spheres you own)
     */
    protected final void changeMoveDirection(final Map<Sphere, Position> spheres) {
        Stream<Entry<Sphere, Position>> tmp = spheres.entrySet()
                                         .stream()
                                         .filter(e -> ownSpheres.contains(e.getKey()));

        currentChangeDest = () -> tmp.forEach(e -> physics.changeDirection(e.getKey(), e.getValue()));
    }

    /**
     * Splits the given (own) sphere to two equally (half) sized ones. This works
     * only if the maximal amount of spheres at the same time is not already reached
     * and will not be overflown with the given map of spheres to be splitted.
     * Otherwise this will simply do nothing.
     *
     * Execution order of merging, splitting and changing direction is at first
     * mine, then merge, then split and then change directions. All steps are
     * independant and need not to be set in every turn. Also splitting can not
     * be done on just merged spheres, as well as new spheres (by splitting)
     * cannot change direction in this turn.
     *
     * @param spheres The spheres you want to split into two parts
     */
    protected final void split(Collection<Sphere> spheres) {
        Stream<Sphere> tmp = spheres.stream().filter(s -> ownSpheres.contains(s));

        if (ownSpheres.size() + spheres.size() <= constants.getMaxSphereAmount()) {
            // lists cannot be changed directly therefore we need the phyiscsmanager here
            currentSplit = () -> tmp.forEach(s -> physics.split(s));
        }
    }

    /**
     * Merges the given (own) spheres to one that has the accumulated size of both.
     *
     * Execution order of merging, splitting and changing direction is at first
     * mine, then merge, then split and then change directions. All steps are
     * independant and need not to be set in every turn. Also splitting can not
     * be done on just merged spheres, as well as new spheres (by splitting)
     * cannot change direction in this turn.
     *
     * @param spheres The map of spheres that should grow to spheres that should vanish
     */
    protected final void merge(Map<Sphere, Sphere> spheres) {
        Stream<Entry<Sphere, Sphere>> tmp = spheres.entrySet()
                                                   .stream()
                                                   .filter(e -> ownSpheres.contains(e.getKey())
                                                                && ownSpheres.contains(e.getValue()));

        // lists cannot be changed directly therefore we need the phyiscsmanager here
        currentMerge = () -> tmp.forEach(e -> physics.merge(e.getKey(),
                                                            e.getValue()));
    }

    /**
     * Mines the given sphere (value) with the other given sphere (key). A sphere
     * can be mined if it is an enemy and only if it is smaller than oneself. If
     * these constraints are not hold this method has no effect for the certain
     * sphere.
     *
     * Execution order of merging, splitting and changing direction is at first
     * mine, then merge, then split and then change directions. All steps are
     * independant and need not to be set in every turn. Also splitting can not
     * be done on just merged spheres, as well as new spheres (by splitting)
     * cannot change direction in this turn.
     *
     * @param spheres The map of (own) spheres to (enemy) spheres that should be mined 
     */
    protected final void mine(Map<Sphere, Sphere> spheres) {
        Stream<Entry<Sphere, Sphere>> tmp = spheres.entrySet().stream()
                                                   .filter(e -> ownSpheres.contains(e.getKey())
                                                                 && !ownSpheres.contains(e.getValue()));
       // tmp.forEach(e -> System.out.println(e));
        currentMine = () -> tmp.forEach(e -> physics.mine(e.getKey(),
                                                          e.getValue()));
    }

    /**
     * Returns the enemies surrounding the given (owned!) sphere in a certain distance.
     *
     * @param sphere The sphere you want to find the surrounding enemies for
     * @return the sourrounding enemies of the given sphere
     */
    protected final Set<Sphere> getSurroundingEnemies(Sphere sphere) {
        if (!ownSpheres.contains(sphere)) {
            return Collections.emptySet();
        }

        return allSpheres.stream()
                         .filter(s -> s.getOwner() != ownAI
                                      && s.getPosition().dist(sphere.getPosition())
                                              <= 
                                          constants.getSightDistance())
                         .collect(Collectors.toSet());
    }

    /**
     * Package private, this should only be called by AIManager!
     * @return indicates wether the turn could be evaluated within the timelimit
     *         or not
     */
    boolean evaluateTurn() {
        setUpTurn();
        Future<?> future = threadExecutor.submit(() -> playTurn());
        try {
            future.get(constants.getAIComputationTime(), TimeUnit.MILLISECONDS);
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            future.cancel(true);
            // TODO proved exception information in logger (introduce logging first)
            System.out.println("could not compute whole turn for: " + ownAI);
         //   e.printStackTrace();
            return false;
        }

        currentMine.apply();
        currentMerge.apply();
        currentSplit.apply();
        currentChangeDest.apply();
        return true;
    }

    private void setUpTurn() {
        // reset turns to evaluate
        currentChangeDest = () -> {};
        currentMerge = () -> {};
        currentSplit = () -> {};
        currentMine = () -> {};

        // reset all sphere related variables
        dots = physics.getDots();
        allSpheres = physics.getAISpheres();
        ownSpheres = allSpheres.stream().filter(p -> p.getOwner() == ownAI).collect(Collectors.toSet());
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
