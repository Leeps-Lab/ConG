package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.config.Config;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    float[] marginalCosts = new float[]{0, 25, 50, 30};

    @Override
    public float getPayoff(
            int id, float percent,
            Map<Integer, float[]> popStrategies,
            Map<Integer, float[]> matchPopStrategies,
            Config config) {
        float minPrice = Float.POSITIVE_INFINITY;
        Set<Integer> minIDs = new HashSet<Integer>();
        for (int i : popStrategies.keySet()) {
            float price = (popStrategies.get(i)[0] * max) - min;
            if (equalPrices(price, minPrice)) {
                minIDs.add(i);
            } else if (price < minPrice) {
                minIDs.clear();
                minPrice = price;
                minIDs.add(i);
            }
        }
        float profit = 0;
        if (minIDs.contains(id)) {
            profit = (minPrice - config.marginalCost) / minIDs.size();
            if (profit < 0) {
                profit = 0;
            }
        }
        return profit;
    }

    private boolean equalPrices(float p1, float p2) {
        return Math.abs(p1 - p2) < 0.5;
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
