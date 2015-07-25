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
    List<Sphere> dots;

    /**
     * Create a new Tick.
     *
     * @param map the map of players to their list of spheres for the tick
     * @param dots the list of dots on the playground
     */
    public Tick(Map<Player, List<Sphere>> map, List<Sphere> dots) {
        this.allSpheres = map;
        this.dots = dots;
    }

    /**
     * Returns the map of players to their list of spheres for this tick.
     *
     * @return the spheresmap
     */
    public Map<Player, List<Sphere>> getSpheresMap() {
        return Collections.unmodifiableMap(allSpheres);
    }

    /**
     * Returns the list of dots on the playground.
     *
     * @return the list of dots
     */
    public List<Sphere> getDots() {
        return Collections.unmodifiableList(dots);
    }
}
