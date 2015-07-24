package me.stieglmaier.sphereMiners.model.ai;

import me.stieglmaier.sphereMiners.model.MutableSphere;
import me.stieglmaier.sphereMiners.model.Player;
import me.stieglmaier.sphereMiners.model.Position;
import me.stieglmaier.sphereMiners.model.Sphere;
import me.stieglmaier.sphereMiners.model.physics.PhysicsManager;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.sosy_lab.common.Pair;


public abstract class SphereMiners2015 {
    /** All owned spheres */
    protected Stream<Sphere> ownSpheres;

    private PhysicsManager physMgr;
    private Player aiName;
    private Map<Player, List<MutableSphere>> allSpheres;
    private Stream<Pair<Sphere, MutableSphere>> sphereMap;
    private Turn currentTurn;

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
        currentTurn = () -> spheres.forEach((sphere, dir) -> sphereMap
                                   .filter(p -> p.getFirst() == sphere)
                                   .findFirst()
                                   .get()
                                   .getSecond()
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
        currentTurn = () -> physMgr.split(sphereMap.filter(p -> p.getFirst() == sphere)
                                                   .findFirst().get().getSecond(),
                                          aiName);
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
        currentTurn = () -> physMgr.merge(sphereMap.filter(p -> p.getFirst() == sphere1)
                                                   .findFirst().get().getSecond(),
                                          sphereMap.filter(p -> p.getFirst() == sphere2)
                                                   .findFirst().get().getSecond(),
                                          aiName);
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
     */
    void evaluateTurn() {
        playTurn();
        currentTurn.apply();
    }

    /**
     * Package private, this should only be called and set by AImanager!
     */
    void setManager(PhysicsManager mgr) {
        physMgr = mgr;
        allSpheres = mgr.getAISpheres();
        sphereMap = allSpheres.get(aiName).stream()
                              .map(sphere -> Pair.of(sphere.toImmutableSphere(), sphere));
        ownSpheres = sphereMap.map(p -> p.getFirst());
    }

    /**
     * Set the name used as identifier in maps throughout the framework
     */
    void setPlayer(Player name) {
        this.aiName = name;
    }

    @FunctionalInterface
    private interface Turn {
        void apply();
    }
}
