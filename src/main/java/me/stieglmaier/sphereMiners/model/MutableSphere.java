package me.stieglmaier.sphereMiners.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    public MutableSphere(Constants constants) {
        this.constants = constants;
        size = constants.getInitialSphereSize();
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
    public List<MutableSphere> split() {
        if (size >= constants.getMinSplittingsize()) {
            MutableSphere newSphere = new MutableSphere(constants);
            newSphere.size = size/2;
            newSphere.direction = direction;
            newSphere.position = position;
            size = (size+1)/2;
            return Arrays.asList(this, newSphere);

        } else {
            return Collections.singletonList(this);
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
        return position.dist(otherSphere.getPosition()) <= constants.getMaxMergeDist()
                // TODO more constraints on size?
                && size > otherSphere.getSize();
    }

    /**
     * Create an immutable view of this sphere object;
     */
    public Sphere toImmutableSphere() {
        return new ImmutableSphere(this);
    }

    public Sphere immutableCopy() {
        MutableSphere newSphere = new MutableSphere(constants);
        newSphere.size = size;
        newSphere.direction = direction;
        newSphere.position = position;
        return newSphere.toImmutableSphere();
    }

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
        public Position getDirection() {
            return sphere.getDirection();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Position getPosition() {
            return sphere.getDirection();
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
        public List<MutableSphere> split() {
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
    }
}
