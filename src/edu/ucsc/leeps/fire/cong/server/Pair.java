/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 *
 * @author dev
 */
public class Pair implements Population, Serializable {

    public ClientInterface player1, player2;
    public long periodStartTime;
    public long lastEvalTime;
    private float[] player1LastStrategy, player2LastStrategy;

    public void setMembers(
            List<ClientInterface> members,
            List<Population> populations,
            Map<String, Population> membership) {
        if (members.size() != 2) {
            throw new IllegalArgumentException("Pair must only be given 2 players.");
        }
        player1 = members.get(0);
        player2 = members.get(1);
        populations.add(this);
        membership.put(player1.getFullName(), this);
        membership.put(player2.getFullName(), this);
    }

    public void initialize(long timestamp, PeriodConfig periodConfig) {
        periodStartTime = timestamp;
        lastEvalTime = timestamp;
        updateStrategies();
    }

    private void updatePayoffs(
            ClientInterface client,
            float[] mine, float[] opponent,
            float percent, float percentInStrategyTime, float inStrategyTime,
            PeriodConfig periodConfig) {
        float points = periodConfig.payoffFunction.getPayoff(
                percent, mine, opponent);
        if (!periodConfig.pointsPerSecond) {
            points *= percentInStrategyTime;
        } else {
            points *= inStrategyTime / 1000f;
        }
        client.addToPeriodPoints(points);
    }

    private void updateStrategies() {
        player1LastStrategy = player1.getStrategy();
        player2LastStrategy = player2.getStrategy();
        player1.setOpponentStrategy(player2LastStrategy);
        player2.setOpponentStrategy(player1LastStrategy);
    }

    public void strategyChanged(String name, long timestamp, PeriodConfig periodConfig) {
        long periodTimeElapsed = timestamp - periodStartTime;
        float percent = periodTimeElapsed / (periodConfig.length * 1000f);
        long inStrategyTime = System.currentTimeMillis() - lastEvalTime;
        float percentInStrategyTime = inStrategyTime / (periodConfig.length * 1000f);
        updatePayoffs(
                player1,
                player1LastStrategy, player2LastStrategy,
                percent, percentInStrategyTime, inStrategyTime, periodConfig);
        updatePayoffs(
                player2,
                player2LastStrategy, player1LastStrategy,
                percent, percentInStrategyTime, inStrategyTime, periodConfig);
        updateStrategies();
    }
}
