package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author dev
 */
public interface Population extends Serializable {

    public void configure(Map<Integer, ClientInterface> members);

    public void setPeriodStartTime(long timestamp);

    public void strategyChanged(
            float[] newStrategy,
            float[] targetStrategy,
            Integer id, long timestamp);

    public void logTick(int subperiod, int millisLeft);

    public void endSubperiod(int subperiod);

    public void endPeriod();
}
