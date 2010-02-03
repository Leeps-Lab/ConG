package edu.ucsc.leeps.fire.cong.server;

/**
 *
 * @author jpettit
 */
public interface TwoStrategyPayoffFunction {

    public float getMax();

    public float getMin();

    public float getPayoff(
            float percent,
            float A, float B,
            float a, float b);
}
