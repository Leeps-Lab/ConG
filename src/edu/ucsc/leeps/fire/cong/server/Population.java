/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import java.util.List;
import java.util.Map;

/**
 *
 * @author dev
 */
public interface Population {

    public void setMembers(
            List<ClientInterface> members,
            List<Population> populations,
            Map<Integer, Population> membership);

    public void initialize(long timestamp, PeriodConfig periodConfig);

    public void strategyChanged(float[] newStrategy, Integer id, long timestamp, PeriodConfig periodConfig);

    public void endPeriod(PeriodConfig periodConfig);
}
