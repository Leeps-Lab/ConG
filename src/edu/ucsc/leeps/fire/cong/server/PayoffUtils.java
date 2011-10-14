package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.Client;
import edu.ucsc.leeps.fire.cong.config.Config;
import java.util.Map;

/**
 *
 * @author jpettit
 */
public class PayoffUtils {

    public static float[] getAverageStrategy(int id, Map<Integer, float[]> strategies) {
        Config config;
        if (FIRE.client != null) {
            config = FIRE.client.getConfig();
        } else {
            config = FIRE.server.getConfig();
        }
        float[] average = null;
        if (strategies.isEmpty()) {
            throw new IllegalArgumentException("no strategies given");
        }
        for (int match : strategies.keySet()) {
            if (average == null) {
                average = new float[strategies.get(match).length];
            }
            if (!(config.excludeSelf && id == match)) {
                float[] s = strategies.get(match);
                for (int i = 0; i < average.length; i++) {
                    average[i] += s[i];
                }
            }
        }
        for (int i = 0; i < average.length; i++) {
            if (config.excludeSelf) {
                average[i] /= (strategies.size() - 1);
            } else {
                average[i] /= strategies.size();
            }
        }
        return average;
    }

    /**
     *
     * @return My flow payoff using the current state
     */
    public static float getPayoff() {
        return FIRE.client.getConfig().payoffFunction.getPayoff(
                Client.state.id,
                Client.state.currentPercent,
                Client.state.strategies, Client.state.matchStrategies,
                FIRE.client.getConfig());
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
                Client.state.getFictitiousStrategies(strategy), Client.state.matchStrategies,
                FIRE.client.getConfig());
    }

    /**
     *
     * @param strategy
     * @return The fictional flow payoff for ID playing strategy
     */
    public static float getPayoff(int id, float[] strategy) {
        return FIRE.client.getConfig().payoffFunction.getPayoff(
                id,
                Client.state.currentPercent,
                Client.state.getFictitiousStrategies(id, strategy), Client.state.matchStrategies,
                FIRE.client.getConfig());
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
                Client.state.getFictitiousMatchStrategies(matchStrategy),
                FIRE.client.getConfig());
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
                    Client.state.matchStrategies, Client.state.strategies,
                    FIRE.client.getConfig());
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
                Client.state.getFictitiousStrategies(strategy),
                FIRE.client.getConfig());
    }
}
