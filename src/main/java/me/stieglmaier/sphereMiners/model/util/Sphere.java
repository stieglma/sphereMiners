package me.stieglmaier.sphereMiners.model.util;

import me.stieglmaier.sphereMiners.model.ai.Player;
import javafx.scene.paint.Color;

public interface Sphere {

    /**
     * Sets the direction in which the Sphere is moving.
     * Should only be called by the Framework, no AI interaction permitted.
     *
     * @param direction the Direction one should move to
     */
    void setDirection(Position direction);

    /**
     * Returns the owner of this sphere and null if this is a dot and thus
     * has no owner.
     *
     * @return the owner of the sphere or null
     */
    Player getOwner();

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
     * Returns the sphere's color.
     * @return the color of the sphere
     */
    Color getColor();

    /**
     * Sets the color of the sphere
     *
     * @param color the new color of the sphere
     */
    void setColor(Color color);

    /**
     * Splits the sphere into two equal sized (smaller) parts.
     * Should only be called by the Framework, no AI interaction permitted.
     *
     * @return the new sphere resulting from splitting the caller sphere
     */
    MutableSphere split();

    /**
     * Merges two spheres of the same player two one big sphere.
     * Should only be called by the Framework, no AI interaction permitted.
     *
     * @param sphere the sphere that should be merged into the caller sphere
     */
    void merge(Sphere sphere);

    /**
     * Checks if a sphere can be merged with another.
     *
     * @param sphere the sphere that should be merge into the caller sphere
     * @return indicates if the this merge is possible
     */
    boolean canBeMergedWidth(Sphere sphere);

}