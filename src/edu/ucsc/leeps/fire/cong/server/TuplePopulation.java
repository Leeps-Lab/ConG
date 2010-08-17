package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import java.util.Map;

/**
 * Divides everyone up into groups. The number of members in a group is based on
 * a variable in the config.
 * @author jpettit
 */
public class TuplePopulation implements Population {

    public void setMembers(Map<Integer, ClientInterface> members, Map<Integer, Population> membership) {
    }

    public void initialize(long timestamp) {
    }

    public void strategyChanged(float[] newStrategy, float[] targetStrategy, float[] hoverStrategy_A, float[] hoverStrategy_a, Integer id, long timestamp) {
    }

    public void logTick(int subperiod, int millisLeft) {
    }

    public void endSubperiod(int subperiod) {
    }

    public void endPeriod() {
    }

}
