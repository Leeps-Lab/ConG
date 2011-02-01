package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.config.Config;
import java.util.Map;

/**
 *
 * @author jpettit
 */
public class CournotPayoffFunction extends TwoStrategyPayoffFunction {

    public float A, B, C, D, smin, smax;

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
        float u = ((A - B * sum) * s - C * s) + D;
        return u;
    }

    @Override
    public float[] getPopStrategySummary(int id, float percent, Map<Integer, float[]> popStrategies, Map<Integer, float[]> matchPopStrategies) {
        float[] summary = new float[4];
        int i = 0;
        for (int match : popStrategies.keySet()) {
            summary[i++] = popStrategies.get(match)[0];
        }
        for (; i < summary.length; i++) {
            summary[i] = Float.NaN;
        }
        return summary;
    }

    @Override
    public float[] getMatchStrategySummary(int id, float percent, Map<Integer, float[]> popStrategies, Map<Integer, float[]> matchPopStrategies) {
        float[] summary = new float[4];
        int i = 0;
        for (int match : popStrategies.keySet()) {
            if (match != id) {
                summary[i++] = popStrategies.get(match)[0];
            }
        }
        for (; i < summary.length; i++) {
            summary[i] = Float.NaN;
        }
        return summary;
    }
}
