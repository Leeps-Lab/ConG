package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author dev
 */
public interface Population extends Serializable {

    public void setMembers(
            Map<Integer, ClientInterface> members,
            Map<Integer, Population> membership);

    public void initialize(long timestamp);

    public void strategyChanged(
            float[] newStrategy,
            float[] targetStrategy,
            float[] hoverStrategy_A,
            float[] hoverStrategy_a,
            Integer id, long timestamp);

    public void logTick();

    public void endSubperiod();

    public void endPeriod();
}
