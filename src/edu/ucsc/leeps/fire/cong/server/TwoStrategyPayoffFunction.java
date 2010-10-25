package edu.ucsc.leeps.fire.cong.server;

import java.util.Map;

/**
 *
 * @author jpettit
 */
public class TwoStrategyPayoffFunction implements PayoffFunction {

    public String name;
    public float Aa;
    public float AaStart, AaEnd;
    public float Ab, Ba, Bb;
    public boolean isCounterpart;
    public float min, max;

    public TwoStrategyPayoffFunction() {
        AaStart = Float.NaN;
        AaEnd = Float.NaN;
        isCounterpart = false;
    }

    public float getMax() {
        return max;
    }

    public float getMin() {
        return min;
    }

    public float getPayoff(
            int id, float percent,
            Map<Integer, float[]> popStrategies,
            Map<Integer, float[]> matchPopStrategies) {
        if (!Float.isNaN(AaStart) && !Float.isNaN(AaEnd)) {
            Aa = AaStart + (percent * (AaEnd - AaStart));
        }
        float A, B, a, b;
        A = popStrategies.get(id)[0];
        B = 1 - A;
        a = PayoffFunction.Utilities.getAverageMatchStrategy(id, popStrategies, matchPopStrategies)[0];
        b = 1 - a;
        if (isCounterpart) {
            return A * (a * Aa + b * Ba) + B * (a * Ab + b * Bb);
        } else {
            return A * (a * Aa + b * Ab) + B * (a * Ba + b * Bb);
        }
    }

    @Override
    public String toString() {
        return String.format("%.2f, %.2f, %.2f, %.2f, %b", Aa, Ab, Ba, Bb, isCounterpart);
    }
}
