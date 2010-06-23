package edu.ucsc.leeps.fire.cong.client;

/**
 *
 * @author jpettit
 */
public interface ClientInterface {

    public float[] getStrategy();

    public void setMyStrategy(float[] s);

    public void setCounterpartStrategy(float[] s);

    public void setTwoStrategyHeatmapBuffers(float[][][] payoff, float[][][] counterpartPayoff);
}
