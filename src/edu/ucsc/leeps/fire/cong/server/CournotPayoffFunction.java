package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.config.Config;
import java.util.Map;

/**
 *
 * @author jpettit
 */
public class CournotPayoffFunction extends TwoStrategyPayoffFunction {

    public float A, B, C, smin, smax;

    public CournotPayoffFunction() {
    }

    @Override
    public boolean reverseXAxis() {
        return true;
    }

    @Override
    public float getMax() {
        return max;
    }

    @Override
    public float getMin() {
        return min;
    }

    @Override
    public float getPayoff(int id, float percent, Map<Integer, float[]> popStrategies, Map<Integer, float[]> matchPopStrategies, Config config) {
        float sum = 0;
        for (float[] f : popStrategies.values()) {
            sum += smin + (f[0] * (smax - smin));
        }
        float s = smin + (popStrategies.get(id)[0] * (smax - smin));
        float u = ((A - B * sum) * s - C * s);
        return u;
    }
}
