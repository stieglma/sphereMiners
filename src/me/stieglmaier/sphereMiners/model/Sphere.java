package me.stieglmaier.sphereMiners.model;

import java.util.List;

public interface Sphere {

    /**
     * Sets the direction in which the Sphere is moving.
     * Should only be called by the Framework, no AI interaction permitted.
     */
    void setDirection(Position direction);

    /**
     * Returns the direction in which the Sphere is moving.
     */
    Position getDirection();

    /**
     * Sets the current position of the Sphere.
     * Should only be called by the Framework, no AI interaction permitted.
     */
    void setPosition(Position position);

    /**
     * Returns the current position of the Sphere.
     */
    Position getPosition();

    /**
     * Sets the size of the Sphere.
     * Should only be called by the Framework, no AI interaction permitted.
     */
    void setSize(int size);

    /**
     * Returns the size of the Sphere.
     */
    int getSize();

    /**
     * Splits the sphere into two equal sized (smaller) parts.
     * Should only be called by the Framework, no AI interaction permitted.
     */
    List<MutableSphere> split();

    /**
     * Merges two spheres of the same player two one big sphere.
     * Should only be called by the Framework, no AI interaction permitted.
     */
    void merge(Sphere sphere);

    /**
     * Checks if a sphere can be merged with another.
     * @param sphere2 the sphere to merge
     */
    boolean canBeMergedWidth(Sphere sphere2);

}