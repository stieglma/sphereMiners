package me.stieglmaier.sphereMiners.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Tick {

    Map<Player, List<Sphere>> allSpheres;

    public Tick(Map<Player, List<Sphere>> map) {
        this.allSpheres = map;
    }

    public Map<Player, List<Sphere>> getSpheresMap() {
        return Collections.unmodifiableMap(allSpheres);
    }

    public void print() {
        for (Entry<Player, List<Sphere>> entry : allSpheres.entrySet()) {
            System.out.println("Player " + entry.getKey() + ":");
            System.out.println(entry.getValue());
        }
    }
}
