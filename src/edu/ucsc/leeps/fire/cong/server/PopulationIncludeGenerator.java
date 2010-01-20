/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author jpettit
 */
public class PopulationIncludeGenerator implements StrategySetGenerator, Serializable {

    public float[] getStrategy(ClientInterface player, List<ClientInterface> players) {
        int strategySize = player.getStrategy().length;
        float[] strategy = new float[strategySize];
        for (int i = 0; i < strategySize; i++) {
            strategy[i] = player.getStrategy()[i];
        }
        for (ClientInterface otherPlayer : players) {
            for (int i = 0; i < strategySize; i++) {
                strategy[i] = strategy[i] + otherPlayer.getStrategy()[i];
            }
        }
        int numPlayers = 1 + players.size();
        for (int i = 0; i < strategySize; i++) {
            strategy[i] = strategy[i] / (float) numPlayers;
        }
        return strategy;
    }
}
