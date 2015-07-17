package me.stieglmaier.sphereMiners.model.ai;

import me.stieglmaier.sphereMiners.model.MutableSphere;
import me.stieglmaier.sphereMiners.model.Position;
import me.stieglmaier.sphereMiners.model.Sphere;
import me.stieglmaier.sphereMiners.model.physics.PhysicsManager;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.sosy_lab.common.Pair;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

public abstract class SphereMiners2015 {
    /** All owned spheres */
    protected FluentIterable<Sphere> ownSpheres;

    private Function<Void, Void> actualTurn;
    private PhysicsManager physMgr;
    private String aiName;
    private Map<String, List<MutableSphere>> allSpheres;
    private FluentIterable<Pair<Sphere, MutableSphere>> sphereMap;

    /**
     * A predicate that finds a certain counterpart in fluentiterables filled
     * with pairs (unfortunately there is no fluent map implementation)
     */
    private static final Predicate<Pair<Sphere, MutableSphere>> equalIfFirstPartMatches(Sphere sphere) {
        return new Predicate<Pair<Sphere, MutableSphere>>() {
            @Override
            public boolean apply(Pair<Sphere, MutableSphere> pair) {
                return pair.getFirst() == sphere;
            }
        };
    }

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
        actualTurn = new Function<Void, Void>() {
            @Override
            public Void apply(Void arg0) {
                for (Entry<Sphere, Position> entry : spheres.entrySet()) {
                    sphereMap.firstMatch(equalIfFirstPartMatches(entry.getKey()))
                             .get()
                             .getSecond()
                             .setPosition(entry.getValue());
                }
                return null;
            }
        };
    }

    /**
     * Splits the given sphere to two equally (half) sized ones.
     *
     * Executing this method prevents you from executing {@link SphereMiners2015#changeMoveDirection(Map)}
     * and {@link SphereMiners2015#merge(Sphere, Sphere)} in this turn. The last
     * called method of these will be executed.
     */
    protected final void split(Sphere sphere) {
        actualTurn = new Function<Void, Void>() {
            @Override
            public Void apply(Void arg0) {
                // lists cannot be changed directly therefore we need the phyiscsmanager here
                physMgr.split(sphereMap.firstMatch(equalIfFirstPartMatches(sphere))
                                       .get().getSecond(),
                              aiName);
                return null;
            }
        };
    }

    /**
     * Merges the given spheres to one that has the accumulated size of both.
     *
     * Executing this method prevents you from executing {@link SphereMiners2015#changeMoveDirection(Map)}
     * and {@link SphereMiners2015#split(Sphere)} in this turn. The last
     * called method of these will be executed.
     */
    protected final void merge(Sphere sphere1, Sphere sphere2) {
        actualTurn = new Function<Void, Void>() {
            @Override
            public Void apply(Void arg0) {
                // lists cannot be changed directly therefore we need the phyiscsmanager here
                physMgr.merge(sphereMap.firstMatch(equalIfFirstPartMatches(sphere1))
                                       .get().getSecond(),
                              sphereMap.firstMatch(equalIfFirstPartMatches(sphere2))
                                       .get().getSecond(),
                              aiName);
                return null;
            }
        };
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
        actualTurn.apply(null);
    }

    /**
     * Package private, this should only be called and set by AImanager!
     */
    void setManager(PhysicsManager mgr) {
        physMgr = mgr;
        allSpheres = mgr.getAISpheres();
        sphereMap = FluentIterable.from(allSpheres.get(aiName))
                                  .transform(new Function<MutableSphere, Pair<Sphere, MutableSphere>>() {
            @Override
            public Pair<Sphere, MutableSphere> apply(MutableSphere sphere) {
                return Pair.of(sphere.toImmutableSphere(), sphere);
            }
        });
        ownSpheres = sphereMap.transform(Pair.getProjectionToFirst());
    }

    /**
     * Set the name used as identifier in maps throughout the framework
     */
    void setName(String name) {
        this.aiName = name;
    }

}
