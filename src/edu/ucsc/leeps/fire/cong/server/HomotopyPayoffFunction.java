/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author jpettit
 */
public class HomotopyPayoffFunction implements PayoffFunction, Serializable {

    private float maxPercent;
    private float AaStart, AaEnd;
    private float Ab, Ba, Bb;
    private int numParameters = 6;
    private int numStrategies = 2;
    private StrategySetGenerator strategySetGenerator;

    public void setStrategySetGenerator(StrategySetGenerator strategySetGenerator) {
        this.strategySetGenerator = strategySetGenerator;
    }

    public int getNumStrategies() {
        return numStrategies;
    }

    public int getPayoff(float percent, ClientInterface player, List<ClientInterface> players) {
        float[] strategy = player.getStrategy();
        float[] otherStrategies = strategySetGenerator.getStrategy(player, players);
        if (strategy.length != numStrategies - 1
                || otherStrategies.length != numStrategies - 1) {
            throw new IllegalArgumentException("Incorrect number of strategies "
                    + "for this payoff function.");
        }
        float Aa;
        if (percent <= maxPercent) {
            Aa = AaStart + (percent * (AaEnd - AaStart));
        } else {
            Aa = AaStart + ((1 - percent) * (AaEnd - AaStart));
        }
        float A, B, a, b;
        A = strategy[0];
        B = 1 - A;
        a = otherStrategies[0];
        b = 1 - a;
        return Math.round(A * (a * Aa + b * Ab) + B * (a * Ba + b * Bb));
    }

    public int getNumParameters() {
        return numParameters;
    }

    public void setParameters(List<Float> parameters) {
        if (parameters.size() == numParameters) {
            maxPercent = parameters.remove(0);
            AaStart = parameters.remove(0);
            AaEnd = parameters.remove(0);
            Ab = parameters.remove(0);
            Ba = parameters.remove(0);
            Bb = parameters.remove(0);
        }
    }
}
