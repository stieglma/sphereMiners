package me.stieglmaier.sphereMiners.model.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.stieglmaier.sphereMiners.main.Constants;
import me.stieglmaier.sphereMiners.model.ai.Player;
import me.stieglmaier.sphereMiners.model.util.GameSimulation;
import me.stieglmaier.sphereMiners.model.util.Sphere;

public enum WinningConditions {

    /**
     * The game will not end, even if only one AI is left.
     */
    OPEN_END {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasGameEnded(GameSimulation simulation, Constants constants) {
            return false;
        }
        
    },

    /**
     * The game will end if only one AI is left.
     */
    ONE_LEFT {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasGameEnded(GameSimulation simulation, Constants constants) {
            Player firstpPlayer = null;
            for (Sphere s : simulation.getTick(simulation.getSize()-1).getSpheres()) {
                if (firstpPlayer == null) {
                    firstpPlayer = s.getOwner();
                } else if (firstpPlayer != s.getOwner()) {
                    return false;
                }
            }
            winner = Collections.singletonList(firstpPlayer);
            return true;
        }
        
    },

    /**
     * The game will end when any AI reaches a certain size. This AI will then
     * also be the winner.
     */
    SIZE_REACHED {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasGameEnded(GameSimulation simulation, Constants constants) {
            Map<Player, Integer> sizes = computePlayerSizes(simulation);

            List<Player> winners = new ArrayList<>();
            for (Entry<Player, Integer> e : sizes.entrySet()) {
                if (e.getValue() > constants.getTotalSizeToReach()) {
                    winners.add(e.getKey());
                }
            }
            winner = winners;
            return !winners.isEmpty();
        }
    },

    /**
     * The game will end after a certain timespan, the winner is
     * the AI with the largest accumulated size of spheres.
     */
    BIGGEST_AFTER_TIME {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasGameEnded(GameSimulation simulation, Constants constants) {
            if (constants.getTotalGameTime() <= simulation.getSize()/constants.getFramesPerSecond()) {
                Map<Player, Integer> sizes = computePlayerSizes(simulation);

                int maxValue = 0;
                for (Entry<Player, Integer> e : sizes.entrySet()) {
                    if (e.getValue() > maxValue) {
                        maxValue = e.getValue();
                    }
                }
                List<Player> winners = new ArrayList<>();
                for (Entry<Player, Integer> e : sizes.entrySet()) {
                    if (e.getValue() == maxValue) {
                        winners.add(e.getKey());
                    }
                }
                winner = winners;
                return true;
            }
            return false;
        }
        
    };

    private static List<Player> winner = new ArrayList<>();

    /**
     * Checks if the game has ended regarding the chosen winning rule.
     * @param simulation the simulation that should be checked if it has ended
     * @param constants the constants that should be used for computing the result
     *
     * @return indicates whether the game has ended or not
     */
    public abstract boolean hasGameEnded(GameSimulation simulation, Constants constants);

    /**
     * Returns the winner if the game has ended, or an empty list. This method
     * has only correct results if before hasGameEnded was called. Additionally
     * this method can only be called once, before hasGameEnded needs to be
     * called again.
     *
     * @return the list of players who fulfill the given rule
     */
    public List<Player> getWinner() {
        List<Player> tmp = winner;
        winner = Collections.emptyList();
        return tmp;
    }

    private static Map<Player, Integer> computePlayerSizes(GameSimulation simulation) {
        Map<Player, Integer> sizes = new HashMap<>();
        for (Sphere s : simulation.getTick(simulation.getSize()-1).getSpheres()) {
            Player currentPlayer = s.getOwner();
            if (sizes.containsKey(currentPlayer)) {
                sizes.replace(currentPlayer, sizes.get(currentPlayer) + s.getSize());
            } else {
                sizes.put(currentPlayer, 0);
            }
        }
        return sizes;
    }
}
