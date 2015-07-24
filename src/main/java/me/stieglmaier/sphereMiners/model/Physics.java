package me.stieglmaier.sphereMiners.model;

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

@Options(prefix="physics")
public class Physics {

    private final Map<Player, List<MutableSphere>> spheresPerPlayer = new HashMap<>();
    private final Configuration config;
    private AIs ais;

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
    private int calcsPerTick = 5;

    @Option(description="The maximum speed a sphere may have in meter/tick")
    private double maxSpeed = 1.0;

    @Option(description="The minimum speed a sphere may have in meter/tick")
    private double minSpeed = 1.0;

    /**
     * The duration in seconds of one partial tick.
     */
    private final double PART_TICK;
    private static int timePerTick = 0;

    public Physics(Configuration config) throws InvalidConfigurationException {
        config.inject(this);
        this.config = config;
        PART_TICK = tick / calcsPerTick;
        timePerTick = (int) (PART_TICK * 1000);
        System.out.println(timePerTick);
    }

    public void setAIManager(AIs mgr) {
        ais = mgr;
    }

    public Tick createInitialTick(List<Player> playingAIs) {
        Position initalPos = new Position(fieldWidth/2, fieldHeight/2);
        double angle = 360.0/playingAIs.size();
        
        // b² = c² - a², c = 1 im Einheitskreis, a = sin Alpha * c im Einheitskreis
        // Strahlensatz: ZA zu BA = ZA' zu BA', c zu a = x*c zu x*a sodass x*a = 5 
        double a = Math.sin(angle/360*Math.PI);
        double radius = initialDistance / 2 / a;

        int i = 0;
        for (Player ai : playingAIs) {
            ai.setSize(10); // TODO should'nt be hardcoded...

            // create new sphere for current player
            List<MutableSphere> sphereList = new ArrayList<>();
            try {
                MutableSphere sphere = new MutableSphere(config);
                Position addPos = new Position(radius * Math.cos(i * 2 * Math.PI / playingAIs.size()),
                                               radius * Math.sin(i * 2 * Math.PI / playingAIs.size()));
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
            ais.applyMoves();

            // 2. move all spheres
            moveSpheres();

            // 3. merge overlapping spheres of opponent ais
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
                double speed = (maxSize - sphere.getSize()) / maxSize * maxSpeed * PART_TICK;
                Position tmpPos = sphere.getPosition().add(sphere.getDirection().normalize().mult(speed));
                double x = tmpPos.getX() % fieldWidth;
                double y = tmpPos.getY() % fieldHeight;
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

    /**
     * Time per tick in milliseconds
     */
    public static int getTimePerTick() {
        return timePerTick;
    }

}
