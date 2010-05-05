/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import edu.ucsc.leeps.fire.cong.logging.EventLog;
import edu.ucsc.leeps.fire.cong.logging.TickLog;
import java.util.List;
import java.util.Map;

/**
 *
 * @author dev
 */
public interface Population {

    public void setMembers(
            List<ClientInterface> members,
            Map<Integer, Population> membership);

    public void initialize(long timestamp, PeriodConfig periodConfig);

    public void strategyChanged(
            float[] newStrategy,
            float[] targetStrategy,
            float[] hoverStrategy_A,
            float[] hoverStrategy_a,
            Integer id, long timestamp,
            PeriodConfig periodConfig,
            EventLog eventLog);

    public void logTick(TickLog tickLog, PeriodConfig periodConfig);

    public void endPeriod(PeriodConfig periodConfig);
}
