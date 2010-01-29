package edu.ucsc.leeps.fire.cong.server;

/**
 *
 * @author jpettit
 */
public interface TwoStrategyPayoffFunction {

    public float getPayoff(
            float percent,
            float A, float B,
            float a, float b);
}
