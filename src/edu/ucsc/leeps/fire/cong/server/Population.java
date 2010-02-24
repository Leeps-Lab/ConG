/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.server;

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
            Map<String, Population> membership);

    public void initialize(long timestamp, PeriodConfig periodConfig);

    public void strategyChanged(String name, long timestamp, PeriodConfig periodConfig);
}
