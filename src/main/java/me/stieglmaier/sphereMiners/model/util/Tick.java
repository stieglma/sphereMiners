package me.stieglmaier.sphereMiners.model.util;

import java.util.List;

import me.stieglmaier.sphereMiners.model.ai.Player;

/**
 * Tick objects represent the information for one tick (aka frame).
 *
 * @author stieglma
 *
 */
public class Tick {

    private final List<Sphere> allSpheres;
    private final List<Sphere> dots;

    /**
     * Create a new Tick.
     *
     * @param spheres the list of spheres for the tick
     * @param dots the list of dots on the playground
     */
    public Tick(List<Sphere> spheres, List<Sphere> dots) {
        this.allSpheres = spheres;
        this.dots = dots;
    }

    /**
     * Returns the list of spheres for this tick.
     *
     * @return the list of spheres
     */
    public List<Sphere> getSpheres() {
        return allSpheres;
    }

    /**
     * Returns the list of dots on the playground.
     *
     * @return the list of dots
     */
    public List<Sphere> getDots() {
        return dots;
    }

    /**
     * Returns this tick as a WinningTick with the given list of players as winners.
     *
     * @param winners the list of players that won this round
     * @return the created winning tick
     */
    public WinningTick toWinningTick(List<Player> winners){
        return new WinningTick(allSpheres, dots, winners);
    }

    public class WinningTick extends Tick {

        private final List<Player> winners;

        /**
         * Create a new Winning Tick
         *
         * @param map the list of spheres for the tick
         * @param dots the list of dots on the playground
         * @param winners the list of winners
         */
        public WinningTick(List<Sphere> spheres, List<Sphere> dots, List<Player> winners) {
            super(spheres, dots);
            this.winners = winners;
        }

        /**
         * Returns the list players that won this round
         *
         * @return the list of players that won this round
         */
        public List<Player> getWinners() {
            return winners;
        }
    }
}
