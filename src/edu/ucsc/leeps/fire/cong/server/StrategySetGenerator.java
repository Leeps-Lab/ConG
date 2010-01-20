/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import java.util.List;

/**
 *
 * @author jpettit
 */
public interface StrategySetGenerator {

    /*
     * Returns the set of strategies player is facing, can include or not
     * include player's strategy if that is wished. Length of list returned
     * must precisely match the required length of the payoff function
     * this generator is used for.
     */
    public float[] getStrategy(ClientInterface player, List<ClientInterface> players);
}
