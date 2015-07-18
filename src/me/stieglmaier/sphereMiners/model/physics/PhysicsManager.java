package me.stieglmaier.sphereMiners.model.physics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.configuration.Option;
import org.sosy_lab.common.configuration.Options;

import me.stieglmaier.sphereMiners.model.MutableSphere;
import me.stieglmaier.sphereMiners.model.Position;
import me.stieglmaier.sphereMiners.model.Sphere;
import me.stieglmaier.sphereMiners.model.Tick;
import me.stieglmaier.sphereMiners.model.ai.AIManager;

@Options(prefix="physics")
public class PhysicsManager {

    private final Map<String, List<MutableSphere>> spheresPerPlayer = new HashMap<>();
    private final Configuration config;
    private AIManager aiManager;

    @Option(description="The initial distance between the spheres of all ais")
    private int initialDistance = 50;

    @Option(description="The width of the game field in meters")
    private int fieldWidth = 100;

    @Option(description="The height of the game field in meters")
    private int fieldHeight = 100;

    
    @Option(description="The frames that should be displayed per second")
    private int framesPerSecond = 25;

    /**
     * The duration of one tick in seconds.
     */
    @Option(description="The duration of a tick in seconds. "
            + "Changing this changes the game speed!")
    private double tick = 1.0 / framesPerSecond;

    /**
     * The concrete calculation steps per tick.
     */
    @Option(description="The amount of calculations that should be done per tick."
            + " Changing this changes the granularity of the calculations")
    private int calcsPerTick = 10;

    @Option(description="The maximum speed a sphere may have in meter/tick")
    private double maxSpeed = 1.0;

    @Option(description="The minimum speed a sphere may have in meter/tick")
    private double minSpeed = 1.0;

    /**
     * The duration in seconds of one partial tick.
     */
    private final double PART_TICK = tick / calcsPerTick;

    public PhysicsManager(Configuration config) throws InvalidConfigurationException {
        config.inject(this);
        this.config = config;
    }

    public void setAIManager(AIManager mgr) {
        aiManager = mgr;
    }

    public Tick createInitialTick(String[] ais) {
        Position initalPos = new Position(fieldWidth/2, fieldHeight/2);
        double angle = 360.0/ais.length;
        
        // b² = c² - a², c = 1 im Einheitskreis, a = sin Alpha * c im Einheitskreis
        // Strahlensatz: ZA zu BA = ZA' zu BA', c zu a = x*c zu x*a sodass x*a = 5 
        double a = Math.sin(angle/360*Math.PI);
        double radius = initialDistance / 2 / a;

        int i = 0;
        for (String ai : ais) {
            // create new sphere for current player
            List<MutableSphere> sphereList = new ArrayList<>();
            try {
                MutableSphere sphere = new MutableSphere(config);
                Position addPos = new Position(radius * Math.cos(i * 2 * Math.PI / ais.length),
                                               radius * Math.sin(i * 2 * Math.PI / ais.length));
                sphere.setPosition(initalPos.add(addPos));
                sphereList.add(sphere);
            } catch (InvalidConfigurationException e) {
                // this will never happen, hopefully...
                throw new RuntimeException();
            }
            spheresPerPlayer.put(ai, sphereList);
            i++;
        }
        return snapshot();
    }

    public Tick applyPhysics() throws IllegalArgumentException, InterruptedException {
        for (int i = 0; i < calcsPerTick; i++) {
            // 1. let ais do ther moves
            aiManager.applyMoves();

            // 2. move all spheres
            moveSpheres();

            // 3. merge overlapping spheres of opponent ais
            mergeSpheres();
        }
        return snapshot();
    }

    private Tick snapshot() {
        Map<String, List<Sphere>> newMap = new HashMap<>();
        for (Entry<String, List<MutableSphere>> entry : spheresPerPlayer.entrySet()) {
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
                double speed = (maxSize - sphere.getSize()) / maxSize * maxSpeed * PART_TICK;
                Position tmpPos = sphere.getPosition().add(sphere.getDirection().normalize().mult(speed));
                double x = tmpPos.getX() % fieldWidth;
                double y = tmpPos.getY() % fieldHeight;
                sphere.setPosition(new Position(x, y));
            }
        }
    }

    private void mergeSpheres() {
        for(Entry<String, List<MutableSphere>> entry : spheresPerPlayer.entrySet()) {
            String playerName = entry.getKey();
            for (MutableSphere playerSphere : entry.getValue()) {
                for(Entry<String, List<MutableSphere>> enemies : spheresPerPlayer.entrySet()) {
                    if (enemies.getKey().equals(playerName)) continue;
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

    public Map<String, List<MutableSphere>> getAISpheres() {
        return Collections.unmodifiableMap(spheresPerPlayer);
    }

    public void split(MutableSphere sphere, String aiName) {
        List<MutableSphere> spheres = spheresPerPlayer.get(aiName);
        spheres.remove(sphere);
        spheres.addAll(sphere.split());
    }

    public void merge(MutableSphere sphere1, MutableSphere sphere2, String aiName) {
        if (sphere1.canBeMergedWidth(sphere2)) {
            List<MutableSphere> spheres = spheresPerPlayer.get(aiName);
            spheres.remove(sphere2);
            sphere1.merge(sphere2);
        }
    }

}
