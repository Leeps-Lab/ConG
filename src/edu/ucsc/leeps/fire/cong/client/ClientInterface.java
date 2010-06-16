package edu.ucsc.leeps.fire.cong.client;

/**
 *
 * @author jpettit
 */
public interface ClientInterface {

    public float[] getStrategy();

    public void initMyStrategy(float[] s);

    public void setMyStrategy(float[] s);

    public void setCounterpartStrategy(float[] s);

    public void setIsCounterpart(boolean isCounterpart);

    public void setTwoStrategyHeatmapBuffers(float[][][] payoff, float[][][] counterpartPayoff);
}
