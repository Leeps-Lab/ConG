/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import edu.ucsc.leeps.fire.cong.logging.EventLog;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jpettit
 */
public class SinglePopulationExclude implements Population, Serializable {

    private List<ClientInterface> members;
    private long periodStartTime;
    private long lastEvalTime;
    private Map<ClientInterface, float[]> lastStrategiesMine;
    private Map<ClientInterface, float[]> lastStrategiesOpposing;

    public SinglePopulationExclude() {
        members = new LinkedList<ClientInterface>();
        lastStrategiesMine = new HashMap<ClientInterface, float[]>();
        lastStrategiesOpposing = new HashMap<ClientInterface, float[]>();
    }

    public void setMembers(
            List<ClientInterface> members,
            List<Population> populations,
            Map<Integer, Population> membership) {
        this.members = members;
        populations.add(this);
        for (ClientInterface client : members) {
            membership.put(client.getID(), this);
        }
    }

    public void initialize(long timestamp, PeriodConfig periodConfig) {
        periodStartTime = timestamp;
        lastEvalTime = timestamp;
        lastStrategiesMine.clear();
        lastStrategiesOpposing.clear();
        updateStrategies(periodConfig);
    }

    private void updatePayoffs(
            ClientInterface client,
            float percent, float percentInStrategyTime, float inStrategyTime,
            PeriodConfig periodConfig) {
        float[] myLast = lastStrategiesMine.get(client);
        float[] otherLast = lastStrategiesOpposing.get(client);
        float points = periodConfig.payoffFunction.getPayoff(
                percent, myLast, otherLast);
        if (!periodConfig.pointsPerSecond) {
            points *= percentInStrategyTime;
        } else {
            points *= inStrategyTime / 1000f;
        }
        client.addToPeriodPoints(points);
    }

    private void updateStrategies(PeriodConfig periodConfig) {
        for (ClientInterface client : members) {
            float[] averageStrategy = null;
            if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
                averageStrategy = new float[1];
                averageStrategy[0] = 0;
            } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
                averageStrategy = new float[3];
                averageStrategy[0] = 0;
                averageStrategy[1] = 0;
                averageStrategy[2] = 0;
            } else {
                assert false;
            }
            for (ClientInterface other : members) {
                if (other != client) {
                    float[] strategy = other.getStrategy();
                    for (int i = 0; i < averageStrategy.length; i++) {
                        averageStrategy[i] += strategy[i];
                    }
                }
            }
            for (int i = 0; i < averageStrategy.length; i++) {
                averageStrategy[i] /= (members.size() - 1);
            }
            client.setCounterpartStrategy(averageStrategy);
            lastStrategiesMine.put(client, client.getStrategy());
            lastStrategiesOpposing.put(client, averageStrategy);
        }
    }

    public void strategyChanged(
            float[] newStrategy,
            float[] targetStrategy,
            float[][] hoverStrategy,
            Integer id, long timestamp,
            PeriodConfig periodConfig,
            EventLog eventLog) {
        long periodTimeElapsed = timestamp - periodStartTime;
        float percent = periodTimeElapsed / (periodConfig.length * 1000f);
        long inStrategyTime = System.currentTimeMillis() - lastEvalTime;
        float percentInStrategyTime = inStrategyTime / (periodConfig.length * 1000f);
        for (ClientInterface client : members) {
            updatePayoffs(
                    client, percent, percentInStrategyTime, percentInStrategyTime, periodConfig);
        }
        lastEvalTime = timestamp;
        updateStrategies(periodConfig);
    }

    public void endPeriod(PeriodConfig periodConfig) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
