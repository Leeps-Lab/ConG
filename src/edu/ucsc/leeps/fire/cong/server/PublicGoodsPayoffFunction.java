package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.config.Config;
import java.util.Map;

/**
 *
 * @author jpettit
 */
public class PublicGoodsPayoffFunction extends TwoStrategyPayoffFunction {

    public float A, B, C;

    public PublicGoodsPayoffFunction() {
    }

    @Override
    public boolean reverseXAxis() {
        return true;
    }

    @Override
    public float getMax() {
        return max * 100;
    }

    @Override
    public float getMin() {
        return min * 100;
    }

    @Override
    public float getPayoff(int id, float percent, Map<Integer, float[]> popStrategies, Map<Integer, float[]> matchPopStrategies, Config config) {
        max = popStrategies.size();
        float sum = 0;
        for (int i : matchPopStrategies.keySet()) {
            if (i != id) {
                sum += matchPopStrategies.get(i)[0];
            }
        }
        float s = popStrategies.get(id)[0];
        sum += s;
        float u = 100 * ((A * sum) + (1 - s));
        if (u < getMin()) {
            return getMin();
        } else if (u > getMax()) {
            return getMax();
        }
        return u;
    }
}
