package edu.ucsc.leeps.fire.cong.client;

import java.util.Map;

/**
 *
 * @author jpettit
 */
public interface ClientInterface {

    public boolean haveInitialStrategy();

    public float getCost();

    public void newMessage(String s, int i);

    public void setStrategies(Map<Integer, float[]> strategies);

    public void setMatchStrategies(Map<Integer, float[]> matchStrategies);

    public void endSubperiod(
            int subperiod,
            Map<Integer, float[]> strategies,
            Map<Integer, float[]> matchStrategies,
            float payoff, float matchPayoff);
}
