package me.stieglmaier.sphereMiners.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.stieglmaier.sphereMiners.main.Constants;


public class Physics {

    private final Map<Player, List<MutableSphere>> spheresPerPlayer = new HashMap<>();

    private final Constants constants;
    private final double tickLength;
    private final double partialTick;

    public Physics(Constants constants) {
        this.constants = constants;
        tickLength = 1.0 / constants.getFramesPerSecond();
        partialTick = tickLength / constants.getCalcsPerTick();
    }

    public Tick createInitialTick(List<Player> playingAIs) {
        Position initalPos = new Position(constants.getFieldWidth()/2, constants.getFieldHeight()/2);
        double angle = 360.0/playingAIs.size();
        
        // b² = c² - a², c = 1 im Einheitskreis, a = sin Alpha * c im Einheitskreis
        // Strahlensatz: ZA zu BA = ZA' zu BA', c zu a = x*c zu x*a sodass x*a = 5 
        double a = Math.sin(angle/360*Math.PI);
        double radius = constants.getInitialDistance() / 2 / a;

        int i = 0;
        for (Player ai : playingAIs) {
            ai.setSize(10); // TODO should'nt be hardcoded...

            // create new sphere for current player
            List<MutableSphere> sphereList = new ArrayList<>();
            MutableSphere sphere = new MutableSphere(constants);
            Position addPos = new Position(radius * Math.cos(i * 2 * Math.PI / playingAIs.size()),
                                           radius * Math.sin(i * 2 * Math.PI / playingAIs.size()));
            sphere.setPosition(initalPos.add(addPos));
            sphereList.add(sphere);

            spheresPerPlayer.put(ai, sphereList);
            i++;
        }
        return snapshot();
    }

    public Tick applyPhysics() throws IllegalArgumentException, InterruptedException {
        for (int i = 0; i <  constants.getCalcsPerTick(); i++) {
            // 1. move all spheres
            moveSpheres();

            // 2. merge overlapping spheres of opponent ais
            mergeSpheres();
        }
        for (Player p : spheresPerPlayer.keySet()) {
            p.setSize(spheresPerPlayer.get(p).stream().map(s -> s.getSize()).reduce(0, (a, b) -> a + b));
        }
        return snapshot();
    }

    private Tick snapshot() {
        Map<Player, List<Sphere>> newMap = new HashMap<>();
        for (Entry<Player, List<MutableSphere>> entry : spheresPerPlayer.entrySet()) {
            ArrayList<Sphere> newList = new ArrayList<>();
            for (MutableSphere sphere : entry.getValue()) {
                newList.add(sphere.immutableCopy());
            }
            newMap.put(entry.getKey(), Collections.unmodifiableList(newList));
        }
        return new Tick(Collections.unmodifiableMap(newMap));
    }

    private void moveSpheres() {
        // get size of biggest sphere
        int maxSize = 0;
        for (List<MutableSphere> spheres : spheresPerPlayer.values()) {
            for (MutableSphere sphere : spheres) {
                if (sphere.getSize() > maxSize) {
                    maxSize = sphere.getSize();
                }
            }
        }
        for (List<MutableSphere> spheres : spheresPerPlayer.values()) {
            for (MutableSphere sphere : spheres) {
                double speed = (constants.getInitialSphereSize()
                                        / sphere.getSize()
                                        * (constants.getMaxSpeed() - constants.getMinSpeed())
                               + constants.getMinSpeed()) * partialTick;
                Position tmpPos = sphere.getPosition().add(sphere.getDirection().normalize().mult(speed));
                // TODO should we have an infinite field? if not rearrange this code and check the borders
                double x = tmpPos.getX() % constants.getFieldWidth();
                double y = tmpPos.getY() % constants.getFieldHeight();
                sphere.setPosition(new Position(x, y));
            }
        }
    }

    private void mergeSpheres() {
        for(Entry<Player, List<MutableSphere>> entry : spheresPerPlayer.entrySet()) {
            Player player = entry.getKey();
            for (MutableSphere playerSphere : entry.getValue()) {
                for(Entry<Player, List<MutableSphere>> enemies : spheresPerPlayer.entrySet()) {
                    if (enemies.getKey().equals(player)) continue;
                    Iterator<MutableSphere> it = enemies.getValue().iterator();
                    while (it.hasNext()) {
                        MutableSphere enemySphere = it.next();
                        if (playerSphere.canBeMergedWidth(enemySphere)) {
                            it.remove();
                            playerSphere.merge(enemySphere);
                        }
                    }
                }
            }
        }
    }

    public Map<Player, List<MutableSphere>> getAISpheres() {
        return Collections.unmodifiableMap(spheresPerPlayer);
    }

    public void split(MutableSphere sphere, Player aiName) {
        List<MutableSphere> spheres = spheresPerPlayer.get(aiName);
        spheres.remove(sphere);
        spheres.addAll(sphere.split());
    }

    public void merge(MutableSphere sphere1, MutableSphere sphere2, Player aiName) {
        if (sphere1.canBeMergedWidth(sphere2)) {
            List<MutableSphere> spheres = spheresPerPlayer.get(aiName);
            spheres.remove(sphere2);
            sphere1.merge(sphere2);
        }
    }

}
