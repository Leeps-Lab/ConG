package edu.ucsc.leeps.fire.cong.server;

import java.util.List;

/**
 *
 * @author jpettit
 */
public interface TwoStrategyPayoffFunction {

    public int getPayoff(float percent, float A, float a);

    public void setParameters(List<Float> parameters);
}