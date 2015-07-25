package me.stieglmaier.sphereMiners.model;

import java.util.List;

public interface Sphere {

    /**
     * Sets the direction in which the Sphere is moving.
     * Should only be called by the Framework, no AI interaction permitted.
     *
     * @param direction the Direction one should move to
     */
    void setDirection(Position direction);

    /**
     * Returns the direction in which the Sphere is moving.
     *
     * @return the direction the sphere is moving
     */
    Position getDirection();

    /**
     * Sets the current position of the Sphere.
     * Should only be called by the Framework, no AI interaction permitted.
     *
     * @param position The new position of the sphere
     */
    void setPosition(Position position);

    /**
     * Returns the current position of the Sphere.
     *
     * @return the current position of the sphere
     */
    Position getPosition();

    /**
     * Sets the size of the Sphere.
     * Should only be called by the Framework, no AI interaction permitted.
     *
     * @param size the new size of the sphere
     */
    void setSize(int size);

    /**
     * Returns the size of the Sphere.
     *
     * @return the current size of the sphere
     */
    int getSize();

    /**
     * Returns the radius of the Sphere.
     *
     * @return the current radius of the sphere
     */
    double getRadius();

    /**
     * Splits the sphere into two equal sized (smaller) parts.
     * Should only be called by the Framework, no AI interaction permitted.
     *
     * @return the list of sphere resulting from splitting the caller sphere
     */
    List<MutableSphere> split();

    /**
     * Merges two spheres of the same player two one big sphere.
     * Should only be called by the Framework, no AI interaction permitted.
     *
     * @param the sphere that should be merged into the caller sphere
     */
    void merge(Sphere sphere);

    /**
     * Checks if a sphere can be merged with another.
     *
     * @param sphere the sphere that should be merge into the caller sphere
     */
    boolean canBeMergedWidth(Sphere sphere);

}