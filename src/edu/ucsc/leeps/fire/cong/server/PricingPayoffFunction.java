package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.config.Config;
import java.util.Map;

/**
 *
 * @author jpettit
 */
public class PricingPayoffFunction extends TwoStrategyPayoffFunction {

    public PricingPayoffFunction() {
        min = 0;
        max = 100;
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
    public float getPayoff(
            int id, float percent,
            Map<Integer, float[]> popStrategies,
            Map<Integer, float[]> matchPopStrategies,
            Config config) {
        float minPrice = Float.POSITIVE_INFINITY;
        for (float[] price : popStrategies.values()) {
            if (price[0] < minPrice) {
                minPrice = price[0];
            }
        }
        if (popStrategies.get(id)[0] == minPrice) {
            return (minPrice * max) - min - config.marginalCost;
        }
        return 0;
    }

    @Override
    public float[] getPopStrategySummary(int id, float percent, Map<Integer, float[]> popStrategies, Map<Integer, float[]> matchPopStrategies) {
        return null;
        //return super.getPopStrategySummary(id, percent, popStrategies, matchPopStrategies);
    }

    @Override
    public float[] getMatchStrategySummary(int id, float percent, Map<Integer, float[]> popStrategies, Map<Integer, float[]> matchPopStrategies) {
        return null;
        //return super.getMatchStrategySummary(id, percent, popStrategies, matchPopStrategies);
    }
}
