package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import java.util.List;

/**
 *
 * @author jpettit
 */
public interface PayoffFunction {

    public int getNumStrategies();

    public int getPayoff(float percent, ClientInterface player, List<ClientInterface> players);

    public void setParameters(List<Float> parameters);
}