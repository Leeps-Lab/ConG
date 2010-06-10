package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.cong.server.ServerInterface;

/**
 *
 * @author jpettit
 */
public interface ClientInterface extends edu.ucsc.leeps.fire.client.ClientInterface<ServerInterface, ClientInterface, PeriodConfig> {

    public float[] getStrategy();

    public void initMyStrategy(float[] s);

    public void setMyStrategy(float[] s);

    public void setCounterpartStrategy(float[] s);

    public void setIsCounterpart(boolean isCounterpart);

    public void setTwoStrategyHeatmapBuffers(float[][][] payoff, float[][][] counterpartPayoff);
}
