package me.stieglmaier.sphereMiners.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Tick objects represent the information for one tick (aka frame).
 *
 * @author stieglma
 *
 */
public class Tick {

    Map<Player, List<Sphere>> allSpheres;

    /**
     * Create a new Tick.
     *
     * @param map the map of players to their list of spheres for the tick
     */
    public Tick(Map<Player, List<Sphere>> map) {
        this.allSpheres = map;
    }

    /**
     * Returns the map of players to their list of spheres for this tick.
     *
     * @return the spheresmap
     */
    public Map<Player, List<Sphere>> getSpheresMap() {
        return Collections.unmodifiableMap(allSpheres);
    }
}
