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

    public float E;

    public PricingPayoffFunction() {
        min = 0;
        max = 100;
        E = Float.NaN;
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
            if (Float.isNaN(E)) {
                profit = (minPrice - config.marginalCost) / minIDs.size();
            } else {
                profit = (((1 - (float) Math.pow(minPrice, E)) / minIDs.size()) - config.marginalCost) * minPrice;
            }
            if (profit < 0) {
                profit = 0;
            }
        }
        return profit;
    }

    private boolean equalPrices(float p1, float p2) {
        return Math.abs(p1 - p2) < 0.5;
    }

    /*
     * Return the min of popStrategies
     */
    @Override
    public float[] getPopStrategySummary(int id, float percent, Map<Integer, float[]> popStrategies, Map<Integer, float[]> matchPopStrategies) {
        return null;
    }

    /*
     * Return an array of the strategies for everyone in popStrategies except id
     */
    @Override
    public float[] getMatchStrategySummary(int id, float percent, Map<Integer, float[]> popStrategies, Map<Integer, float[]> matchPopStrategies) {
        return null;
    }
}
