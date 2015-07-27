package me.stieglmaier.sphereMiners.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import javafx.scene.paint.Color;
import me.stieglmaier.sphereMiners.main.Constants;

/**
 * This class handles all the physical computations such as moving and "eating"
 * smaller spheres that are in the necessary range. The move compuatation is done
 * based on a certain FPS number which is divided into smaller computation parts.
 *
 * @author stieglma
 *
 */
public class Physics {

    private final BiMap<MutableSphere, Sphere> aiSpheres = HashBiMap.create();
    private final Set<MutableSphere> dots = new HashSet<>();

    private final Constants constants;
    private final double tickLength;
    private final double partialTick;
    private final Random random = new Random();
    private final Set<Sphere> spheresForAisNextTurn = new HashSet<>();

    /**
     * Creates a physics object.
     * @param constants The constants object the physics will base the computation on.
     */
    public Physics(Constants constants) {
        this.constants = constants;
        tickLength = 1.0 / constants.getFramesPerSecond();
        partialTick = tickLength / constants.getCalcsPerTick();
    }

    /**
     * Creates the initial tick for a simulation.
     * @param playingAIs the list of players that should take part
     * @return the computed initial tick
     */
    public Tick createInitialTick(List<Player> playingAIs) {
        Position initalPos = new Position(constants.getFieldWidth()/2, constants.getFieldHeight()/2);
        double angle = 360.0/playingAIs.size();
        
        // b² = c² - a², c = 1 im Einheitskreis, a = sin Alpha * c im Einheitskreis
        // Strahlensatz: ZA zu BA = ZA' zu BA', c zu a = x*c zu x*a sodass x*a = 5 
        double a = Math.sin(angle/360*Math.PI);
        double radius = constants.getInitialDistance() / 2 / a;

        int i = 0;
        for (Player ai : playingAIs) {
            // create new sphere for current player
            MutableSphere sphere = new MutableSphere(constants, ai);
            Position addPos = new Position(radius * Math.cos(i * 2 * Math.PI / playingAIs.size()),
                                           radius * Math.sin(i * 2 * Math.PI / playingAIs.size()));

            // place modulo the usual position if it would be out of bounds
            Position spherePos = initalPos.add(addPos);
            if (spherePos.getX() < 0 || spherePos.getX() > constants.getFieldWidth()
                || spherePos.getY() < 0 || spherePos.getY() > constants.getFieldHeight()) {
                spherePos = new Position(spherePos.getX() % constants.getFieldWidth(),
                                         spherePos.getY() % constants.getFieldHeight());
            }

            sphere.setPosition(spherePos);

            aiSpheres.put(sphere, sphere.toImmutableSphere());
            i++;
        }
        createDots(constants.getDotAmount());

        spheresForAisNextTurn.addAll(aiSpheres.values());
        return snapshot();
    }

    private void createDots(int number) {
        for (int i = 0; i < number; i++) {
            MutableSphere sphere = new MutableSphere(constants);
            sphere.setPosition(new Position(random.nextInt(constants.getFieldWidth()+1),
                                            random.nextInt(constants.getFieldHeight()+1)));
            sphere.setSize(constants.getDotSize());
            sphere.setColor(new Color(random.nextDouble(), random.nextDouble(), random.nextDouble(), 1));
            dots.add(sphere);
        }
    }

    /**
     * This method applies the physics for one tick (frame). It is computed
     * in smaller parts in a loop, the last of these parts is then packed
     * into a tick and returned.
     *
     * @return the computed tick
     */
    public Tick applyPhysics() {
        for (int i = 0; i <  constants.getCalcsPerTick(); i++) {
            // 1. move all spheres
            moveSpheres();

            // 2. merge dots into spheres
            mergeDots();
        }

        // refill dots
        createDots(constants.getDotAmount() - dots.size());

        // update ailist
        spheresForAisNextTurn.clear();
        spheresForAisNextTurn.addAll(aiSpheres.values());

        return snapshot();
    }

    private Tick snapshot() {
        Builder<Sphere> sphereCopy = ImmutableList.builder();
        for (MutableSphere sphere : aiSpheres.keySet()) {{
                sphereCopy.add(sphere.immutableCopy());
            }
        }

        Builder<Sphere> dotsCopy = ImmutableList.builder();
        for (MutableSphere dot : dots) {
            dotsCopy.add(dot.immutableCopy());
        }

        return new Tick(sphereCopy.build(), dotsCopy.build());
    }

    private void moveSpheres() {
        for (MutableSphere sphere : aiSpheres.keySet()) {
            double speed = (Math.log(constants.getInitialSphereSize())
                                    / Math.log(sphere.getSize())
                                    * (constants.getMaxSpeed() - constants.getMinSpeed())
                           + constants.getMinSpeed()) * partialTick;

            Position tmpPos = sphere.getPosition().add(sphere.getDirection().mult(speed));

            double x = tmpPos.getX() > constants.getFieldWidth() ? constants.getFieldWidth() : 
                            (tmpPos.getX() < 0 ? 0 : tmpPos.getX());
            double y = tmpPos.getY() > constants.getFieldHeight() ? constants.getFieldHeight() : 
                (tmpPos.getY() < 0 ? 0 : tmpPos.getY());

            sphere.setPosition(new Position(x, y));
        }
    }

    private void mergeDots() {
        for(MutableSphere sphere : aiSpheres.keySet()) {
            Iterator<MutableSphere> dotsIt = dots.iterator();
            while(dotsIt.hasNext()) {
                Sphere dot = dotsIt.next();
                if (sphere.canBeMergedWidth(dot)) {
                    sphere.merge(dot);
                    dotsIt.remove();
                }
            }
        }
    }

    /**
     * Returns a set of the map with all spheres per player.
     *
     * @return a set of all spheres owned by AIs
     */
    public Set<Sphere> getAISpheres() {
        return spheresForAisNextTurn;
    }

    /**
     * Returns an unmodifiable view of the set of all dots on the playground
     *
     * @return the set of all dots on the playground
     */
    public Set<Sphere> getDots() {
        return Collections.unmodifiableSet(dots.stream().map(s -> s.toImmutableSphere()).collect(Collectors.toSet()));
    }

    public void changeDirection(Sphere sphere, Position direction) {
        aiSpheres.inverse().get(sphere).setDirection(direction.normalize());
    }

    /**
     * Splits a sphere into two smaller parts
     * @param sphere the sphere to split
     */
    public void split(Sphere sphere) {
        MutableSphere s = aiSpheres.inverse().get(sphere);
        MutableSphere newSphere = s.split();
        if (newSphere != null) {
            aiSpheres.put(newSphere, newSphere.toImmutableSphere());
        }
    }

    /**
     * Merges two spheres if they are in the necessary range to do that.
     * @param big the sphere that should grow
     * @param small the sphere that should be merged into the other one
     */
    public void merge(Sphere big, Sphere small) {
        if (big.canBeMergedWidth(small)) {
            MutableSphere bigger = aiSpheres.inverse().get(big);
            MutableSphere smaller = aiSpheres.inverse().get(small);
            aiSpheres.remove(smaller);
            bigger.merge(smaller);
        }
    }

    public void mine(Sphere minerSphere, Sphere minedSphere) {
        if (minerSphere.canBeMergedWidth(minedSphere)) {
            MutableSphere miner = aiSpheres.inverse().get(minerSphere);
            MutableSphere mined = aiSpheres.inverse().get(minedSphere);
            aiSpheres.remove(mined);
            miner.merge(mined);
        }
    }
}
