package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.client.ClientState;
import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.cong.logging.EventLog;
import edu.ucsc.leeps.fire.cong.logging.TickLog;
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

    private List<ClientState> members;
    private long periodStartTime;
    private long lastEvalTime;
    private Map<ClientState, float[]> lastStrategiesMine;
    private Map<ClientState, float[]> lastStrategiesOpposing;

    public SinglePopulationExclude() {
        members = new LinkedList<ClientState>();
        lastStrategiesMine = new HashMap<ClientState, float[]>();
        lastStrategiesOpposing = new HashMap<ClientState, float[]>();
    }

    public void setMembers(
            List<ClientState> members,
            Map<Integer, Population> membership) {
        this.members = members;
        for (ClientState client : members) {
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
            ClientState client,
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
        for (ClientState client : members) {
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
            for (ClientState other : members) {
                if (other != client) {
                    float[] strategy = other.client.getStrategy();
                    for (int i = 0; i < averageStrategy.length; i++) {
                        averageStrategy[i] += strategy[i];
                    }
                }
            }
            for (int i = 0; i < averageStrategy.length; i++) {
                averageStrategy[i] /= (members.size() - 1);
            }
            client.client.setCounterpartStrategy(averageStrategy);
            lastStrategiesMine.put(client, client.client.getStrategy());
            lastStrategiesOpposing.put(client, averageStrategy);
        }
    }

    public void strategyChanged(
            float[] newStrategy,
            float[] targetStrategy,
            float[] hoverStrategy_A,
            float[] hoverStrategy_a,
            Integer id, long timestamp,
            PeriodConfig periodConfig,
            EventLog eventLog) {
        long periodTimeElapsed = timestamp - periodStartTime;
        float percent = periodTimeElapsed / (periodConfig.length * 1000f);
        long inStrategyTime = System.currentTimeMillis() - lastEvalTime;
        float percentInStrategyTime = inStrategyTime / (periodConfig.length * 1000f);
        for (ClientState client : members) {
            updatePayoffs(
                    client, percent, percentInStrategyTime, percentInStrategyTime, periodConfig);
        }
        lastEvalTime = timestamp;
        updateStrategies(periodConfig);
    }

    public void endPeriod(PeriodConfig periodConfig) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void logTick(TickLog tickLog, PeriodConfig periodConfig) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
