package me.stieglmaier.sphereMiners.model.ai;

import me.stieglmaier.sphereMiners.model.MutableSphere;
import me.stieglmaier.sphereMiners.model.Position;
import me.stieglmaier.sphereMiners.model.Sphere;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;

public abstract class SphereMiners2015 {
    protected List<MutableSphere> spheres;
    private Function<Void, Void> actualTurn;
    private AIManager aiMgr;

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

    protected final void changeMoveDirection(final Map<MutableSphere, Position> sphere) {
        actualTurn = new Function<Void, Void>() {
            @Override
            public Void apply(Void arg0) {
                throw new UnsupportedOperationException("not implemented");
            }
        };
    }

    protected final void split(Sphere sphere) {
        actualTurn = new Function<Void, Void>() {
            @Override
            public Void apply(Void arg0) {
                spheres.remove(sphere);
                spheres.addAll(sphere.split());
                return null;
            }
        };
    }

    protected final void merge(Sphere sphere1, Sphere sphere2) {
        actualTurn = new Function<Void, Void>() {
            @Override
            public Void apply(Void arg0) {
                if (sphere1.canBeMergedWidth(sphere2)) {
                    spheres.remove(sphere2);
                    sphere1.merge(sphere2);
                }
                return null;
            }
        };
    }

    // TODO implement this operation
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
    void setManager(AIManager mgr) {
        aiMgr = mgr;
    }

    void setSpheres(List<MutableSphere> spheres) {
        this.spheres = spheres;
    }
}
