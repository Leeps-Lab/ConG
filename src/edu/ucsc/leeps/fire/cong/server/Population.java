package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.config.Configurable;
import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import edu.ucsc.leeps.fire.cong.logging.EventLog;
import edu.ucsc.leeps.fire.cong.logging.TickLog;
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
            Integer id, long timestamp,
            EventLog eventLog);

    public void logTick(TickLog tickLog);

    public void endPeriod();
}
