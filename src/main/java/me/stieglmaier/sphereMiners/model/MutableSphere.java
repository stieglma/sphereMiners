package me.stieglmaier.sphereMiners.model;

import javafx.scene.paint.Color;
import me.stieglmaier.sphereMiners.main.Constants;


/**
 * Be careful sphere is a mutable class. Only an
 * immutable view should be passed to AIs.
 * 
 */
public class MutableSphere implements Sphere {

    private final Constants constants;
    private Position position;
    private Position direction = new Position();
    private int size;
    private Color color;
    private final Player owner;

    /**
     * Creatae a new Mutable sphere with the relevant constants
     *
     * @param owner the player who owns this sphere
     * @param constants the constants to use for this sphere
     */
    public MutableSphere(Constants constants, Player owner) {
        this.constants = constants;
        this.owner = owner;
        size = constants.getInitialSphereSize();
    }

    /**
     * Creatae a new Mutable sphere with the relevant constants
     *
     * @param owner the player who owns this sphere
     * @param constants the constants to use for this sphere
     */
    public MutableSphere(Constants constants) {
        this.constants = constants;
        this.owner = null;
        size = constants.getInitialSphereSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getOwner() {
        return owner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDirection(Position direction) {
        this.direction = direction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Position getDirection() {
        return direction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Position getPosition() {
        return position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getRadius() {
        return Math.sqrt(size/Math.PI);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getColor() {
        return color;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSize() {
        return this.size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MutableSphere split() {
        if (size >= constants.getMinSplittingsize()) {
            MutableSphere newSphere = new MutableSphere(constants, owner);
            newSphere.size = size/2;
            newSphere.direction = direction;
            newSphere.position = position;
            size = (size+1)/2;
            return newSphere;

        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void merge(Sphere sphere) {
        if (canBeMergedWidth(sphere)) {
            size += sphere.getSize();
            sphere.setSize(0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canBeMergedWidth(Sphere otherSphere) {
        return position.dist(otherSphere.getPosition()) - (getRadius() + otherSphere.getRadius()) <= constants.getMinMergeDist()
                // TODO more constraints on size?
                && size > otherSphere.getSize();
    }

    /**
     * Create an immutable view of this sphere object.
     *
     * @return an immutable view of the sphere
     */
    public Sphere toImmutableSphere() {
        return new ImmutableSphere(this);
    }

    /**
     * Creates an immutable copy of this sphere object.
     *
     * @return an immutable copy of this sphere
     */
    public Sphere immutableCopy() {
        MutableSphere newSphere = new MutableSphere(constants, owner);
        newSphere.size = size;
        newSphere.direction = direction;
        newSphere.position = position;
        newSphere.color = color;
        return newSphere.toImmutableSphere();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "Sphere (" + size + ") at " + position;
    }

    private static class ImmutableSphere implements Sphere {
        Sphere sphere;

        private ImmutableSphere(Sphere sphere) {
            this.sphere = sphere;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Player getOwner() {
            return sphere.getOwner();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Position getDirection() {
            return sphere.getDirection();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Position getPosition() {
            return sphere.getPosition();
        }

        /**
         * {@inheritDoc}
         */
        public double getRadius() {
            return sphere.getRadius();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getSize() {
            return sphere.getSize();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Color getColor() {
            return sphere.getColor();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setColor(Color color) {
            throw new UnsupportedOperationException("This object is immutable!");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setSize(int size) {
            throw new UnsupportedOperationException("This object is immutable!");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setDirection(Position direction) {
            throw new UnsupportedOperationException("This object is immutable!");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setPosition(Position position) {
            throw new UnsupportedOperationException("This object is immutable!");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public MutableSphere split() {
            throw new UnsupportedOperationException("This object is immutable!");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void merge(Sphere sphere) {
            throw new UnsupportedOperationException("This object is immutable!");
        }

       /**
        * {@inheritDoc}
        */
       @Override
       public boolean canBeMergedWidth(Sphere sphere2) {
           return sphere.canBeMergedWidth(sphere2);
       }

       public String toString() {
           return "Sphere (" + sphere.getSize() + ") at " + sphere.getPosition();
       }
    }
}
