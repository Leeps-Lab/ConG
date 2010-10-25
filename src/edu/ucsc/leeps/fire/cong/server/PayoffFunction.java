package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.Client;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author dev
 */
public interface PayoffFunction extends Serializable {

    public float getMin();

    public float getMax();

    public float getPayoff(
            int id, float percent,
            Map<Integer, float[]> popStrategies,
            Map<Integer, float[]> matchPopStrategies);

    public static class Utilities {

        public static float[] getAverageMatchStrategy(
                int id,
                Map<Integer, float[]> popStrategies,
                Map<Integer, float[]> matchPopStrategies) {
            boolean excludeSelf = false;
            float[] average = null;
            for (int match : matchPopStrategies.keySet()) {
                if (average == null) {
                    average = new float[matchPopStrategies.get(match).length];
                }
                if (!(excludeSelf && id == match)) {
                    for (int i = 0; i < average.length; i++) {
                        average[i] += matchPopStrategies.get(match)[i];
                    }
                }
            }
            for (int i = 0; i < average.length; i++) {
                if (excludeSelf) {
                    average[i] /= (matchPopStrategies.size() - 1);
                } else {
                    average[i] /= matchPopStrategies.size();
                }
            }
            return average;
        }

        public static float[] getAverageMatchStrategy() {
            return getAverageMatchStrategy(Client.state.id,
                    Client.state.strategies, Client.state.matchStrategies);
        }

        /**
         *
         * @return My flow payoff using the current state
         */
        public static float getPayoff() {
            return FIRE.client.getConfig().payoffFunction.getPayoff(
                    Client.state.id,
                    Client.state.currentPercent,
                    Client.state.strategies, Client.state.matchStrategies);
        }

        /**
         *
         * @param strategy
         * @return The fictional flow payoff if I was playing strategy
         */
        public static float getPayoff(float[] strategy) {
            return FIRE.client.getConfig().payoffFunction.getPayoff(
                    Client.state.id,
                    Client.state.currentPercent,
                    Client.state.getFictitiousStrategies(strategy), Client.state.matchStrategies);
        }

        /**
         *
         * @param strategy
         * @param matchStrategy
         * @return The fictional flow payoff if I was playing strategy and the
         * average of my matched population was matchStrategy
         */
        public static float getPayoff(float[] strategy, float[] matchStrategy) {
            return FIRE.client.getConfig().payoffFunction.getPayoff(
                    Client.state.id,
                    Client.state.currentPercent,
                    Client.state.getFictitiousStrategies(strategy),
                    Client.state.getFictitiousMatchStrategies(matchStrategy));
        }

        /**
         *
         * @return The average flow payoff of my matched population, using the current state
         */
        public static float getMatchPayoff() {
            float payoff = 0;
            for (Integer matchID : Client.state.matchStrategies.keySet()) {
                payoff += FIRE.client.getConfig().counterpartPayoffFunction.getPayoff(
                        matchID,
                        Client.state.currentPercent,
                        Client.state.matchStrategies, Client.state.strategies);
            }
            return payoff / Client.state.matchStrategies.size();
        }

        /**
         *
         * @param strategy
         * @param matchStrategy
         * @return The average fictitious flow payoff of my matched population
         */
        public static float getMatchPayoff(float[] strategy, float[] matchStrategy) {
            return FIRE.client.getConfig().counterpartPayoffFunction.getPayoff(
                    Client.state.id,
                    Client.state.currentPercent,
                    Client.state.getFictitiousMatchStrategies(matchStrategy),
                    Client.state.getFictitiousStrategies(strategy));
        }
    }
}
