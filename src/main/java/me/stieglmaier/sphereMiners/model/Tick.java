package me.stieglmaier.sphereMiners.model;

import java.util.List;

/**
 * Tick objects represent the information for one tick (aka frame).
 *
 * @author stieglma
 *
 */
public class Tick {

    private List<Sphere> allSpheres;
    private List<Sphere> dots;

    /**
     * Create a new Tick.
     *
     * @param map the list of spheres for the tick
     * @param dots the list of dots on the playground
     */
    public Tick(List<Sphere> spheres, List<Sphere> dots) {
        this.allSpheres = spheres;
        this.dots = dots;
    }

    /**
     * Returns the list of spheres for this tick.
     *
     * @return the list of spheres
     */
    public List<Sphere> getSpheresMap() {
        return allSpheres;
    }

    /**
     * Returns the list of dots on the playground.
     *
     * @return the list of dots
     */
    public List<Sphere> getDots() {
        return dots;
    }
}
