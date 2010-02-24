/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author jpettit
 */
public class SinglePopulationInclude implements Population {

    public List<ClientInterface> members;
    public long lastEvalTime;
    public float averageStrategy_a;
    public float averageStrategy_r;
    public float averageStrategy_p;
    public float averageStrategy_s;

    public SinglePopulationInclude() {
        members = new LinkedList<ClientInterface>();
    }

    public void strategyChanged(String name, long timestamp) {
        
    }
}
